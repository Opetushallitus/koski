package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.log.Logging
import org.json4s.JValue

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val todistusService = application.todistusService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.todistusFeatureFlags.isServiceEnabled) return None

    schedulerInstance = Some(new Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.cleanupInterval")),
      None,
      runNext,
      intervalMillis = 1000,
      concurrency = 1
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
  }

  private def runNext(_ignore: Option[JValue]): Option[JValue] = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("todistus")
    todistusService.cleanup(activeWorkers)
    None
  }
}
