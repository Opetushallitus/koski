package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import org.postgresql.util.{PSQLException, PSQLState}

import java.time.LocalDateTime
import java.util.UUID

class KielitutkintotodistusTiedoteService(application: KoskiApplication) extends Logging with Timing {
  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val client = application.tiedotuspalveluClient
  private val maxAttempts = application.config.getInt("tiedote.maxAttempts")
  private val batchSize = application.config.getInt("tiedote.batchSize")

  def processAll(): Int = {
    timed("processAll", thresholdMs = 0) {
      var processed = 0
      var eligibleBatch = repository.findEligibleBatch(batchSize)

      while (eligibleBatch.nonEmpty) {
        eligibleBatch.foreach { case (opiskeluoikeusOid, oppijaOid) =>
          processOne(opiskeluoikeusOid, oppijaOid)
        }
        processed += eligibleBatch.size
        eligibleBatch = repository.findEligibleBatch(batchSize)
      }

      logger.info(s"processAll valmis: käsitelty $processed tiedotetta")
      processed
    }
  }

  def processNext(): Unit = {
    repository.findNextEligible.foreach { case (opiskeluoikeusOid, oppijaOid) =>
      processOne(opiskeluoikeusOid, oppijaOid)
    }
  }

  private def processOne(opiskeluoikeusOid: String, oppijaOid: String): Unit = {
    logger.info(s"Lähetetään tiedote: oppija=$oppijaOid oo=$opiskeluoikeusOid")

    val (state, error) = client.sendKielitutkintoTodistusTiedote(oppijaOid, opiskeluoikeusOid) match {
      case Right(()) =>
        (KielitutkintotodistusTiedoteState.COMPLETED, None)
      case Left(err) =>
        (KielitutkintotodistusTiedoteState.ERROR, Some(err.toString))
    }

    val job = KielitutkintotodistusTiedoteJob(
      id = UUID.randomUUID().toString,
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      state = state,
      completedAt = if (state == KielitutkintotodistusTiedoteState.COMPLETED) Some(LocalDateTime.now()) else None,
      worker = Some(repository.workerId),
      attempts = 1,
      error = error
    )

    try {
      repository.add(job)
      if (state == KielitutkintotodistusTiedoteState.COMPLETED) {
        logger.info(s"Tiedote lähetetty: oo=$opiskeluoikeusOid")
      } else {
        logger.error(s"Tiedotteen lähetys epäonnistui: oo=$opiskeluoikeusOid virhe=${error.getOrElse("")}")
      }
    } catch {
      case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
        logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle $opiskeluoikeusOid, ohitetaan")
      case e: Exception =>
        logger.error(e)(s"Tiedotteen tallennus epäonnistui opiskeluoikeudelle $opiskeluoikeusOid")
        throw e
    }
  }

  def retryAllFailed(): Int = {
    timed("retryAllFailed", thresholdMs = 0) {
      val retryable = repository.findAllRetryable(maxAttempts)
      retryable.foreach(retryOne)
      logger.info(s"retryAllFailed valmis: käsitelty ${retryable.size} uudelleenyritystä")
      retryable.size
    }
  }

  def retryFailed(): Unit = {
    repository.findNextRetryable(maxAttempts).foreach(retryOne)
  }

  private def retryOne(job: KielitutkintotodistusTiedoteJob): Unit = {
    logger.info(s"Yritetään tiedotetta uudelleen: job=${job.id} yritys=${job.attempts}/$maxAttempts")

    client.sendKielitutkintoTodistusTiedote(job.oppijaOid, job.opiskeluoikeusOid) match {
      case Right(()) =>
        repository.setCompleted(job.id)
        logger.info(s"Tiedote lähetetty uudelleenyrityksellä: job=${job.id}")
      case Left(error) =>
        repository.setFailed(job.id, error.toString)
        logger.error(s"Tiedotteen uudelleenyritys epäonnistui: job=${job.id} yritys=${job.attempts}/$maxAttempts virhe=$error")
    }
  }
}
