package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Schedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class KielitutkintotodistusTiedoteScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kielitutkintotodistus-tiedote"
  val tiedoteService: KielitutkintotodistusTiedoteService = application.kielitutkintotodistusTiedoteService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.config.getBoolean("tiedote.enabled")) return None

    val schedule: Schedule = if (application.config.hasPath("tiedote.checkInterval")) {
      new IntervalSchedule(application.config.getDuration("tiedote.checkInterval"))
    } else {
      new IntervalSchedule(Duration.ofHours(1))
    }

    schedulerInstance = Some(new Scheduler(
      application,
      schedulerName,
      schedule,
      None,
      runBatch,
      intervalMillis = 1000,
      concurrency = 1
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
