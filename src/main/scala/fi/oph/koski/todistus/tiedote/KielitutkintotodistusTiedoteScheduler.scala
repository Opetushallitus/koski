package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.GlobalIntervalScheduler

class KielitutkintotodistusTiedoteScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kielitutkintotodistus-tiedote"
  val tiedoteService: KielitutkintotodistusTiedoteService = application.kielitutkintotodistusTiedoteService

  var schedulerInstance: Option[GlobalIntervalScheduler] = None

  def createScheduler: Option[GlobalIntervalScheduler] = {
    if (!application.config.getBoolean("tiedote.enabled")) return None

    schedulerInstance = Some(GlobalIntervalScheduler(
      application,
      schedulerName,
      application.config.getDuration("tiedote.checkInterval"),
      runBatch,
      shouldFireCheckIntervalMillis = 1000
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown())
  }

  private def runBatch(): Unit = {
    tiedoteService.retryAllFailed()
    tiedoteService.processAll()
  }
}
