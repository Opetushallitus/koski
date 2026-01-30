package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.log.Logging
import org.json4s.JValue

import java.time.Duration

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val schedulerDb = application.masterDatabase.db
  val todistusService = application.todistusService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.cleanupInterval")),
      None,
      runNext,
      runOnSingleNode = true,
      intervalMillis = 1000,
      config = application.config
    ))
    schedulerInstance
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(schedulerDb, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(schedulerDb, schedulerName)

  private def runNext(_ignore: Option[JValue]): Option[JValue] = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("todistus")

    todistusService.cleanup(activeWorkers)

    None
  }
}
