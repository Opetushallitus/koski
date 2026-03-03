package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class TodistusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus"
  val todistusService: TodistusService = application.todistusService

  sys.addShutdownHook {
    todistusService.markAllMyJobsInterrupted()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    schedulerInstance = Some(new Scheduler(
      application,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.checkInterval")),
      None,
      runNextTodistus,
      intervalMillis = 1000,
      concurrency = 1
    ))
    schedulerInstance
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(application.masterDatabase.db, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(application.masterDatabase.db, schedulerName)

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
  }

  private def runNextTodistus(_context: Option[JValue]): Option[JValue] = {
    if (todistusService.hasNext) {
      todistusService.runNext()
    }

    None
  }
}
