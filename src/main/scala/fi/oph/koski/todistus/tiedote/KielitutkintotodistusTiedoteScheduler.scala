package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{FixedTimeOfDaySchedule, IntervalSchedule, Scheduler}
import org.json4s.JValue

class KielitutkintotodistusTiedoteScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kielitutkintotodistus-tiedote"
  val schedulerDb = application.masterDatabase.db
  val tiedoteService: KielitutkintotodistusTiedoteService = application.kielitutkintotodistusTiedoteService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.config.getBoolean("tiedote.enabled")) return None

    val schedule = if (application.config.hasPath("tiedote.checkInterval")) {
      new IntervalSchedule(application.config.getDuration("tiedote.checkInterval"))
    } else {
      new FixedTimeOfDaySchedule(
        application.config.getInt("tiedote.schedule.hour"),
        application.config.getInt("tiedote.schedule.minute")
      )
    }

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      schedule,
      None,
      runBatch,
      runOnSingleNode = true,
      intervalMillis = 1000,
      config = application.config
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
  }

  private def runBatch(_context: Option[JValue]): Option[JValue] = {
    tiedoteService.processAll()
    tiedoteService.retryAllFailed()
    None
  }
}
