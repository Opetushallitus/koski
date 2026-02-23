package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, WorkerLeaseElector}
import org.json4s.JValue

import java.time.Duration

class KielitutkintotodistusTiedoteScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kielitutkintotodistus-tiedote"
  val schedulerDb = application.masterDatabase.db
  val tiedoteService: KielitutkintotodistusTiedoteService = application.kielitutkintotodistusTiedoteService

  private val leaseElector = new WorkerLeaseElector(
    application.workerLeaseRepository,
    schedulerName,
    application.instanceId,
    slots = 1,
    leaseDuration = application.config.getDuration("tiedote.workerLease.duration"),
    heartbeatInterval = application.config.getDuration("tiedote.workerLease.heartbeatInterval")
  )

  sys.addShutdownHook {
    leaseElector.shutdown()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.config.getBoolean("tiedote.enabled")) return None

    leaseElector.start(
      onAcquired = _ => logger.info(s"Acquired $schedulerName lease"),
      onLost = _ => logger.warn(s"Lost $schedulerName lease")
    )

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(Duration.ofHours(1)),
      None,
      runBatch,
      intervalMillis = 1000,
      config = application.config
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
    leaseElector.shutdown()
  }

  private def runBatch(_context: Option[JValue]): Option[JValue] = {
    if (leaseElector.hasLease) {
      tiedoteService.processAll()
      tiedoteService.retryAllFailed()
    }
    None
  }
}
