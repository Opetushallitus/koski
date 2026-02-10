package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing

import java.util.UUID

class KielitutkintotodistusTiedoteService(application: KoskiApplication) extends Logging with Timing {
  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val client = application.tiedotuspalveluClient
  private val maxAttempts = application.config.getInt("tiedote.maxAttempts")

  def hasNext: Boolean = repository.findNextEligible.isDefined

  def processNext(): Unit = {
    repository.findNextEligible.foreach { case (opiskeluoikeusOid, oppijaOid) =>
      timed("processNext", thresholdMs = 0) {
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
          case _: org.postgresql.util.PSQLException =>
            logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle $opiskeluoikeusOid, ohitetaan")
        }
      }
    }
  }

  def retryFailed(): Unit = {
    repository.findNextRetryable(maxAttempts).foreach { job =>
      timed("retryFailed", thresholdMs = 0) {
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
  }
}
