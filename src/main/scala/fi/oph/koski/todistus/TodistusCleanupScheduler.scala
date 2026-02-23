package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, WorkerLeaseElector}
import fi.oph.koski.log.Logging
import org.json4s.JValue

import java.time.Duration

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val schedulerDb = application.masterDatabase.db
  val todistusService = application.todistusService

  private val leaseElector = new WorkerLeaseElector(
    application.workerLeaseRepository,
    schedulerName,
    application.instanceId,
    slots = 1,
    leaseDuration = application.config.getDuration("todistus.cleanupWorkerLease.duration"),
    heartbeatInterval = application.config.getDuration("todistus.cleanupWorkerLease.heartbeatInterval")
  )

  sys.addShutdownHook {
    leaseElector.shutdown()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    leaseElector.start(
      onAcquired = _ => logger.info(s"Acquired $schedulerName lease"),
      onLost = _ => logger.warn(s"Lost $schedulerName lease")
    )

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.cleanupInterval")),
      None,
      runNext,
      intervalMillis = 1000,
      config = application.config
    ))
    schedulerInstance
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(schedulerDb, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(schedulerDb, schedulerName)

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
    leaseElector.shutdown()
  }

  private def runNext(_ignore: Option[JValue]): Option[JValue] = {
    if (leaseElector.hasLease) {
      val activeWorkers = application.workerLeaseRepository.activeHolders("todistus")
      todistusService.cleanup(activeWorkers)
    }

    None
  }
}
