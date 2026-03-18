package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{opiskeluoikeusOid => opiskeluoikeusOidField, oppijaHenkiloOid, tiedoteTyyppi}
import fi.oph.koski.log.KoskiOperation.TIEDOTE_LAHETETTY
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.todistus.{TodistusState, TodistusTemplateVariant}
import fi.oph.koski.util.Timing
import org.postgresql.util.{PSQLException, PSQLState}

import java.util.UUID
import scala.util.{Failure, Success, Try}

class KielitutkintotodistusTiedoteService(application: KoskiApplication) extends Logging with Timing {
  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val todistusService = application.todistusService
  private val client = application.tiedotuspalveluClient
  private val kituClient = application.kituClient
  private val maxAttempts = application.config.getInt("tiedote.maxAttempts")
  private val batchSize = application.config.getInt("tiedote.batchSize")
  private val pollIntervalMs = application.config.getInt("tiedote.todistusPollIntervalMs")
  private val pollTimeoutMs = application.config.getInt("tiedote.todistusPollTimeoutMs")

  def processAll(): Int = {
    timed("processAll", thresholdMs = 0) {
      var processed = 0
      var eligibleBatch = repository.findEligibleBatch(batchSize)

      while (eligibleBatch.nonEmpty) {
        eligibleBatch.foreach { case (opiskeluoikeusOid, oppijaOid, versionumero) =>
          processOne(opiskeluoikeusOid, oppijaOid, versionumero)
        }
        processed += eligibleBatch.size
        eligibleBatch = repository.findEligibleBatch(batchSize)
      }

      logger.info(s"processAll valmis: käsitelty $processed tiedotetta")
      processed
    }
  }

  private def processOne(opiskeluoikeusOid: String, oppijaOid: String, versionumero: Int): Unit = {
    logger.info(s"Lähetetään tiedote: oppija=$oppijaOid oo=$opiskeluoikeusOid")

    val tiedoteJobId = UUID.randomUUID().toString
    val tiedoteJob = KielitutkintotodistusTiedoteJob(
      id = tiedoteJobId,
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      state = KielitutkintotodistusTiedoteState.WAITING_FOR_TODISTUS,
      worker = Some(repository.workerId),
      attempts = 1,
      opiskeluoikeusVersio = versionumero
    )

    Try(repository.add(tiedoteJob)) match {
      case Success(_) =>
        generateAndSend(tiedoteJobId, oppijaOid, opiskeluoikeusOid, s"$opiskeluoikeusOid-initial")
      case Failure(e: PSQLException) if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
        logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle $opiskeluoikeusOid, ohitetaan")
      case Failure(e) =>
        logger.error(e)(s"Tiedotteen tallennus epäonnistui opiskeluoikeudelle $opiskeluoikeusOid")
        throw e
    }
  }

  private def generateAndSend(tiedoteJobId: String, oppijaOid: String, opiskeluoikeusOid: String, idempotencyKey: String): Unit = {
    val result = for {
      examineeDetails <- kituClient.getExamineeDetails(opiskeluoikeusOid)
      templateVariant <- examineeDetails.todistuskieli
        .map(kieli => s"${kieli.koodiarvo.toLowerCase}_tulostettava_uusi")
        .filter(TodistusTemplateVariant.printVariants.contains)
        .toRight(KoskiErrorCategory.internalError(s"Tutkinnon suorittajan kielelle ei löydy tulostettavaa todistuspohjaa: ${examineeDetails.todistuskieli.map(_.koodiarvo)}"))

      todistusJob <- todistusService.createTodistusJobForSystem(opiskeluoikeusOid, oppijaOid, templateVariant)
      _ = repository.setState(tiedoteJobId, KielitutkintotodistusTiedoteState.WAITING_FOR_TODISTUS)
      _ = logger.info(s"TodistusJob luotu tiedotteelle: tiedote=$tiedoteJobId todistus=${todistusJob.id}")

      completedTodistus <- pollForTodistusCompletion(todistusJob.id)

      location <- todistusService.copyToTiedoteBucket(completedTodistus.id)
      _ = logger.info(s"Printtitodistus kopioitu tiedote-buckettiin: tiedote=$tiedoteJobId todistus=${completedTodistus.id} bucket=${location.bucket} key=${location.key}")

      _ <- client.sendKielitutkintoTodistusTiedote(oppijaOid, idempotencyKey, location.bucket, location.key)
    } yield completedTodistus

    result match {
      case Right(todistus) =>
        AuditLog.log(KoskiAuditLogMessage(TIEDOTE_LAHETETTY, KoskiSpecificSession.systemUser, Map(
          oppijaHenkiloOid -> oppijaOid,
          opiskeluoikeusOidField -> opiskeluoikeusOid,
          tiedoteTyyppi -> "kielitodistus"
        )))
        repository.setCompleted(tiedoteJobId, todistus.opiskeluoikeusVersionumero.getOrElse(0))
        logger.info(s"Tiedote lähetetty: tiedote=$tiedoteJobId oo=$opiskeluoikeusOid")
      case Left(err) =>
        repository.setFailed(tiedoteJobId, err.toString)
        logger.error(s"Tiedotteen lähetys epäonnistui: tiedote=$tiedoteJobId oo=$opiskeluoikeusOid virhe=$err")
    }
  }

  private def pollForTodistusCompletion(todistusJobId: String): Either[HttpStatus, fi.oph.koski.todistus.TodistusJob] = {
    val deadline = System.currentTimeMillis() + pollTimeoutMs
    var result: Option[Either[HttpStatus, fi.oph.koski.todistus.TodistusJob]] = None

    while (result.isEmpty && System.currentTimeMillis() < deadline) {
      todistusService.getJobStatus(todistusJobId) match {
        case Right(job) if job.state == TodistusState.COMPLETED =>
          result = Some(Right(job))
        case Right(job) if job.state == TodistusState.ERROR =>
          result = Some(Left(KoskiErrorCategory.internalError(s"TodistusJob epäonnistui: ${job.error.getOrElse("tuntematon virhe")}")))
        case Left(err) =>
          result = Some(Left(err))
        case _ =>
          Thread.sleep(pollIntervalMs)
      }
    }

    result.getOrElse(Left(KoskiErrorCategory.internalError(s"TodistusJob $todistusJobId ei valmistunut ${pollTimeoutMs}ms aikana")))
  }

  def retryAllFailed(): Int = {
    timed("retryAllFailed", thresholdMs = 0) {
      val retryable = repository.findAllRetryable(maxAttempts)
      retryable.foreach(retryOne)
      logger.info(s"retryAllFailed valmis: käsitelty ${retryable.size} uudelleenyritystä")
      retryable.size
    }
  }

  private def retryOne(job: KielitutkintotodistusTiedoteJob): Unit = {
    logger.info(s"Yritetään tiedotetta uudelleen: job=${job.id} yritys=${job.attempts}/$maxAttempts")
    generateAndSend(job.id, job.oppijaOid, job.opiskeluoikeusOid, job.opiskeluoikeusOid)
  }
}
