package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, SchedulerMode}

class MassaluovutusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "massaluovutus"
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  sys.addShutdownHook {
    massaluovutukset.cancelAllTasks("Interrupted: worker shutdown")
  }

  var schedulerInstance: Option[Scheduler] = None

  def scheduler: Option[Scheduler] = {
    schedulerInstance = Some(Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
      runNextQuery,
      intervalMillis = 1000,
      mode = SchedulerMode.leaseControlledWithIndependentSchedules(concurrency)
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
