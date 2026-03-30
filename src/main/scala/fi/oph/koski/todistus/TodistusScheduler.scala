package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.IndependentIntervalScheduler


class TodistusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus"
  val todistusService: TodistusService = application.todistusService

  sys.addShutdownHook {
    todistusService.markAllMyJobsInterrupted()
  }

  var schedulerInstance: Option[IndependentIntervalScheduler] = None

  def createScheduler: Option[IndependentIntervalScheduler] = {
    if (!application.todistusFeatureFlags.isServiceEnabled) return None

    schedulerInstance = Some(IndependentIntervalScheduler(
      application,
      schedulerName,
      application.config.getDuration("todistus.checkInterval"),
      runNextTodistus,
      shouldFireCheckIntervalMillis = 1000,
      concurrency = application.config.getInt("todistus.concurrency")
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
