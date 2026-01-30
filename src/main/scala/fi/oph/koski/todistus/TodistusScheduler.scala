package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, WorkerLeaseElector}
import org.json4s.JValue

import java.time.Duration

class TodistusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus"
  val schedulerDb = application.masterDatabase.db
  val todistusService: TodistusService = application.todistusService
  private val leaseElector = new WorkerLeaseElector(
    application.workerLeaseRepository,
    schedulerName,
    application.instanceId,
    slots = 1,
    leaseDuration = application.config.getDuration("todistus.workerLease.duration"),
    heartbeatInterval = application.config.getDuration("todistus.workerLease.heartbeatInterval")
  )

  sys.addShutdownHook {
    leaseElector.shutdown()
    todistusService.markAllMyJobsInterrupted()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    leaseElector.start(
      onAcquired = _ => logger.info(s"Acquired todistus lease (workerId: ${application.todistusRepository.workerId})"),
      onLost = _ => logger.warn(s"Lost todistus lease (workerId: ${application.todistusRepository.workerId})")
    )

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.checkInterval")),
      None,
      runNextTodistus,
      runOnSingleNode = false,
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

  private def runNextTodistus(_context: Option[JValue]): Option[JValue] = {
    if (isTodistusWorker) {
      if (todistusService.hasNext) {
        todistusService.runNext()
      }
    }

    None
  }

  private def isTodistusWorker = leaseElector.hasLease
}
