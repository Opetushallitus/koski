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
        val job = KielitutkintotodistusTiedoteJob(
          id = UUID.randomUUID().toString,
          oppijaOid = oppijaOid,
          opiskeluoikeusOid = opiskeluoikeusOid,
          state = KielitutkintotodistusTiedoteState.SENDING,
          worker = Some(repository.workerId),
          attempts = 1
        )

        val savedJob = try {
          repository.add(job)
        } catch {
          case _: org.postgresql.util.PSQLException =>
            // UNIQUE-constraint violation: tiedote on jo luotu tälle opiskeluoikeudelle
            logger.info(s"Tiedote on jo olemassa opiskeluoikeudelle $opiskeluoikeusOid, ohitetaan")
            return
        }

        logger.info(s"Lähetetään tiedote: job=${savedJob.id} oppija=$oppijaOid oo=$opiskeluoikeusOid")

        client.sendKielitutkintoTodistusTiedote(oppijaOid, opiskeluoikeusOid) match {
          case Right(()) =>
            repository.setCompleted(savedJob.id)
            logger.info(s"Tiedote lähetetty: job=${savedJob.id}")
          case Left(error) =>
            repository.setFailed(savedJob.id, error.toString)
            logger.error(s"Tiedotteen lähetys epäonnistui: job=${savedJob.id} virhe=$error")
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
