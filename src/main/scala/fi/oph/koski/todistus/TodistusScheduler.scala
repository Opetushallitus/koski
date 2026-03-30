package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, SchedulerMode}


class TodistusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus"
  val todistusService: TodistusService = application.todistusService

  sys.addShutdownHook {
    todistusService.markAllMyJobsInterrupted()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.todistusFeatureFlags.isServiceEnabled) return None

    schedulerInstance = Some(Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.checkInterval")),
      runNextTodistus,
      intervalMillis = 1000,
      mode = SchedulerMode.leaseControlledWithIndependentSchedules(application.config.getInt("todistus.concurrency"))
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown())
  }

  private def runNextTodistus(): Unit = {
    if (todistusService.hasNext) {
      todistusService.runNext()
    }
  }
}
