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
  private val stuckThresholdMinutes = application.config.getInt("tiedote.stuckThresholdMinutes")

  def processAll(): Int = {
    timed("processAll", thresholdMs = 0) {
      var processed = 0
      var eligibleBatch = repository.findEligibleBatch(batchSize)

      while (eligibleBatch.nonEmpty) {
        eligibleBatch.foreach(processOne)
        processed += eligibleBatch.size
        eligibleBatch = repository.findEligibleBatch(batchSize)
      }

      logger.info(s"processAll valmis: käsitelty $processed tiedotetta")
      processed
    }
  }

  private def processOne(eligible: KielitutkintotodistusTiedoteEligible): Unit = {
    logger.info(s"Lähetetään tiedote: oppija=${eligible.oppijaOid} oo=${eligible.opiskeluoikeusOid}")

    val tiedoteJobId = UUID.randomUUID().toString
    val tiedoteJob = KielitutkintotodistusTiedoteJob(
      id = tiedoteJobId,
      oppijaOid = eligible.oppijaOid,
      opiskeluoikeusOid = eligible.opiskeluoikeusOid,
      lähdejärjestelmänId = eligible.lähdejärjestelmänId,
      state = KielitutkintotodistusTiedoteState.WAITING_FOR_TODISTUS,
      worker = Some(repository.workerId),
      attempts = 0,
      opiskeluoikeusVersio = eligible.opiskeluoikeusVersio
    )

    Try(repository.add(tiedoteJob)) match {
      case Success(_) =>
        generateAndSend(tiedoteJob, attempt = 1)
      case Failure(e: PSQLException) if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
        logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle ${eligible.opiskeluoikeusOid}, ohitetaan")
      case Failure(e) =>
        logger.error(e)(s"Tiedotteen tallennus epäonnistui opiskeluoikeudelle ${eligible.opiskeluoikeusOid}")
        throw e
    }
  }

  private def generateAndSend(job: KielitutkintotodistusTiedoteJob, attempt: Int): Unit = {
    val kituResult = getKituExamineeDetails(job.opiskeluoikeusOid, job.lähdejärjestelmänId)

    kituResult match {
      case Left(err) if attempt < maxAttempts =>
        repository.setFailed(job.id, s"Kitu-kutsu epäonnistui: $err")
        logger.warn(s"Yhteystietojen haku kitulta epäonnistui, yritetään myöhemmin uudelleen: tiedote=${job.id} oo=${job.opiskeluoikeusOid} yritys=$attempt/$maxAttempts virhe=$err")

      case _ =>
        val examineeDetails = kituResult match {
          case Right(details) => Some(details)
          case Left(err) =>
            logger.warn(s"Yhteystietojen haku kitulta epäonnistui $maxAttempts yrityksen jälkeen, lähetetään tiedote ilman yhteystietoja: tiedote=${job.id} oo=${job.opiskeluoikeusOid} virhe=$err")
            None
        }

        val todistusResult = examineeDetails.flatMap(details => generateTodistus(job.id, job.oppijaOid, job.opiskeluoikeusOid, details))

        val result = client.sendKielitutkintoTodistusTiedote(
          job.oppijaOid,
          job.opiskeluoikeusOid,
          s"${job.opiskeluoikeusOid}-initial",
          todistusResult.map(_.location.bucket),
          todistusResult.map(_.location.key),
          examineeDetails
        )

        result match {
          case Right(_) =>
            AuditLog.log(KoskiAuditLogMessage(TIEDOTE_LAHETETTY, KoskiSpecificSession.systemUser, Map(
              oppijaHenkiloOid -> job.oppijaOid,
              opiskeluoikeusOidField -> job.opiskeluoikeusOid,
              tiedoteTyyppi -> "kielitodistus"
            )))
            val opiskeluoikeusVersio = todistusResult.flatMap(_.opiskeluoikeusVersionumero).getOrElse(0)
            repository.setCompleted(job.id, opiskeluoikeusVersio)
            logger.info(s"Tiedote lähetetty: tiedote=${job.id} oo=${job.opiskeluoikeusOid} printtitodistusPdf=${if (todistusResult.isDefined) "kyllä" else "ei"}")
          case Left(err) =>
            repository.setFailed(job.id, err.toString)
            logger.error(s"Tiedotteen lähetys epäonnistui: tiedote=${job.id} oo=${job.opiskeluoikeusOid} virhe=$err")
        }
    }
  }

  private def getKituExamineeDetails(opiskeluoikeusOid: String, lähdejärjestelmänId: Option[String]): Either[HttpStatus, KituExamineeDetails] = {
    lähdejärjestelmänId.filter(_.nonEmpty) match {
      case Some(id) =>
        kituClient.getExamineeDetails(id)
      case None =>
        logger.warn(s"lähdejärjestelmänId.id puuttuu tiedotejobista, haetaan yhteystiedot Kitulta opiskeluoikeuden oidilla: oo=$opiskeluoikeusOid")
        kituClient.getExamineeDetailsByOpiskeluoikeusOid(opiskeluoikeusOid)
    }
  }

  private def generateTodistus(tiedoteJobId: String, oppijaOid: String, opiskeluoikeusOid: String, examineeDetails: KituExamineeDetails): Option[GeneratedTodistus] = {
    val result = for {
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
    } yield GeneratedTodistus(location, completedTodistus.opiskeluoikeusVersionumero)

    result match {
      case Right(todistus) => Some(todistus)
      case Left(err) =>
        logger.warn(s"Printtitodistuksen generointi epäonnistui, lähetetään tiedote ilman printtitodistusta: tiedote=$tiedoteJobId oo=$opiskeluoikeusOid virhe=$err")
        None
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
      val retryable = repository.findAllRetryable(maxAttempts, stuckThresholdMinutes)
      retryable.foreach(retryOne)
      logger.info(s"retryAllFailed valmis: käsitelty ${retryable.size} uudelleenyritystä")
      retryable.size
    }
  }

  private def retryOne(job: KielitutkintotodistusTiedoteJob): Unit = {
    val attempt = job.attempts + 1
    logger.info(s"Yritetään tiedotetta uudelleen: job=${job.id} yritys=$attempt/$maxAttempts")
    generateAndSend(job, attempt = attempt)
  }
}

private case class GeneratedTodistus(
  location: fi.oph.koski.todistus.TiedoteBucketLocation,
  opiskeluoikeusVersionumero: Option[Int]
)
