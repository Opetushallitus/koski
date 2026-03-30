package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.GlobalIntervalScheduler
import fi.oph.koski.log.Logging

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val todistusService = application.todistusService

  var schedulerInstance: Option[GlobalIntervalScheduler] = None

  def createScheduler: Option[GlobalIntervalScheduler] = {
    if (!application.todistusFeatureFlags.isServiceEnabled) return None

    schedulerInstance = Some(GlobalIntervalScheduler(
      application,
      schedulerName,
      application.config.getDuration("todistus.cleanupInterval"),
      runNext,
      shouldFireCheckIntervalMillis = 1000,
      concurrency = 1
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
