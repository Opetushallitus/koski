package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.IndependentIntervalScheduler

class MassaluovutusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "massaluovutus"
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  sys.addShutdownHook {
    massaluovutukset.cancelAllTasks("Interrupted: worker shutdown")
  }

  var schedulerInstance: Option[IndependentIntervalScheduler] = None

  def scheduler: Option[IndependentIntervalScheduler] = {
    schedulerInstance = Some(IndependentIntervalScheduler(
      application,
      schedulerName,
      application.config.getDuration("kyselyt.checkInterval"),
      runNextQuery,
      shouldFireCheckIntervalMillis = 1000,
      concurrency = concurrency
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown())
  }

  private def runNextQuery(): Unit = {
    if (massaluovutukset.hasNext) {
      if (massaluovutukset.systemIsOverloaded) {
        logger.info("System is overloaded, skipping this round")
      } else {
        massaluovutukset.runNext()
      }
    }
  }
}
