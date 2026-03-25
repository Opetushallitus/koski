package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

class MassaluovutusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "massaluovutus"
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  sys.addShutdownHook {
    massaluovutukset.cancelAllTasks("Interrupted: worker shutdown")
  }

  var schedulerInstance: Option[Scheduler] = None

  def scheduler: Option[Scheduler] = {
    schedulerInstance = Some(new Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
      None,
      runNextQuery,
      intervalMillis = 1000,
      concurrency = concurrency
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
  }

  private def runNextQuery(_context: Option[JValue]): Option[JValue] = {
    if (massaluovutukset.hasNext) {
      if (massaluovutukset.systemIsOverloaded) {
        logger.info("System is overloaded, skipping this round")
      } else {
        massaluovutukset.runNext()
      }
    }
    None
  }
}
