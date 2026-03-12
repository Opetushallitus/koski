package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{opiskeluoikeusOid => opiskeluoikeusOidField, oppijaHenkiloOid, tiedoteTyyppi}
import fi.oph.koski.log.KoskiOperation.TIEDOTE_LAHETETTY
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.todistus.{BucketType, TodistusResultRepository, TodistusState, TodistusTemplateVariant}
import fi.oph.koski.util.Timing
import org.postgresql.util.{PSQLException, PSQLState}

import java.util.UUID

class KielitutkintotodistusTiedoteService(application: KoskiApplication) extends Logging with Timing {
  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val todistusService = application.todistusService
  private val todistusRepository = application.todistusRepository
  private val resultRepository = new TodistusResultRepository(application.config)
  private val client = application.tiedotuspalveluClient
  private val kituClient = application.kituClient
  private val maxAttempts = application.config.getInt("tiedote.maxAttempts")
  private val batchSize = application.config.getInt("tiedote.batchSize")
  private val pollIntervalMs = application.config.getInt("tiedote.todistusPollIntervalMs")
  private val pollTimeoutMs = application.config.getInt("tiedote.todistusPollTimeoutMs")

  private val defaultTemplateVariant = TodistusTemplateVariant.fi_tulostettava_uusi

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

    try {
      repository.add(tiedoteJob)
    } catch {
      case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
        logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle $opiskeluoikeusOid, ohitetaan")
        return
      case e: Exception =>
        logger.error(e)(s"Tiedotteen tallennus epäonnistui opiskeluoikeudelle $opiskeluoikeusOid")
        throw e
    }

    generateAndSend(tiedoteJobId, oppijaOid, opiskeluoikeusOid, s"$opiskeluoikeusOid-initial")
  }

  private def generateAndSend(tiedoteJobId: String, oppijaOid: String, opiskeluoikeusOid: String, idempotencyKey: String): Unit = {
    val result = for {
      examineeDetails <- kituClient.getExamineeDetails(oppijaOid)
      templateVariant = examineeDetails.preferredLanguage
        .map(lang => s"${lang}_tulostettava_uusi")
        .filter(TodistusTemplateVariant.printVariants.contains)
        .getOrElse(defaultTemplateVariant)

      todistusJob <- todistusService.createTodistusJobForSystem(opiskeluoikeusOid, oppijaOid, templateVariant)
      _ = repository.setState(tiedoteJobId, KielitutkintotodistusTiedoteState.WAITING_FOR_TODISTUS)
      _ = logger.info(s"TodistusJob luotu tiedotteelle: tiedote=$tiedoteJobId todistus=${todistusJob.id}")

      completedTodistus <- pollForTodistusCompletion(todistusJob.id)

      _ <- resultRepository.copyObject(BucketType.RAW, BucketType.TIEDOTE, completedTodistus.id)
      todistusUrl = resultRepository.getDirectUrl(BucketType.TIEDOTE, completedTodistus.id)
      _ = logger.info(s"Printtitodistus kopioitu tiedote-buckettiin: $todistusUrl")

      _ <- client.sendKielitutkintoTodistusTiedote(oppijaOid, idempotencyKey, todistusUrl)
    } yield ()

    result match {
      case Right(()) =>
        repository.setCompleted(tiedoteJobId)
        AuditLog.log(KoskiAuditLogMessage(TIEDOTE_LAHETETTY, KoskiSpecificSession.systemUser, Map(
          oppijaHenkiloOid -> oppijaOid,
          opiskeluoikeusOidField -> opiskeluoikeusOid,
          tiedoteTyyppi -> "kielitodistus"
        )))
        logger.info(s"Tiedote lähetetty: tiedote=$tiedoteJobId oo=$opiskeluoikeusOid")
      case Left(err) =>
        repository.setFailed(tiedoteJobId, err.toString)
        logger.error(s"Tiedotteen lähetys epäonnistui: tiedote=$tiedoteJobId oo=$opiskeluoikeusOid virhe=$err")
    }
  }

  private def pollForTodistusCompletion(todistusJobId: String): Either[HttpStatus, fi.oph.koski.todistus.TodistusJob] = {
    val deadline = System.currentTimeMillis() + pollTimeoutMs

    while (System.currentTimeMillis() < deadline) {
      todistusRepository.get(todistusJobId) match {
        case Right(job) if job.state == TodistusState.COMPLETED =>
          return Right(job)
        case Right(job) if job.state == TodistusState.ERROR =>
          return Left(KoskiErrorCategory.internalError(s"TodistusJob epäonnistui: ${job.error.getOrElse("tuntematon virhe")}"))
        case _ =>
          Thread.sleep(pollIntervalMs)
      }
    }

    Left(KoskiErrorCategory.internalError(s"TodistusJob $todistusJobId ei valmistunut ${pollTimeoutMs}ms aikana"))
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
