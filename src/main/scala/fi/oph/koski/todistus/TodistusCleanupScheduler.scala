package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, SchedulerMode}
import fi.oph.koski.log.Logging

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val todistusService = application.todistusService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.todistusFeatureFlags.isServiceEnabled) return None

    schedulerInstance = Some(Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.cleanupInterval")),
      runNext,
      intervalMillis = 1000,
      mode = SchedulerMode.leaseControlledWithSharedSchedule(concurrency = 1)
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown())
  }

  private def runNext(): Unit = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("todistus")
    todistusService.cleanup(activeWorkers)
  }
}
