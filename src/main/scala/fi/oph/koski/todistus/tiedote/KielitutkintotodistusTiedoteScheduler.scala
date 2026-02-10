package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, WorkerLeaseElector}
import org.json4s.JValue

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

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    leaseElector.start(
      onAcquired = _ => logger.info(s"Acquired kielitutkintotodistus-tiedote lease (workerId: ${application.kielitutkintotodistusTiedoteRepository.workerId})"),
      onLost = _ => logger.warn(s"Lost kielitutkintotodistus-tiedote lease (workerId: ${application.kielitutkintotodistusTiedoteRepository.workerId})")
    )

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("tiedote.checkInterval")),
      None,
      runNext,
      runOnSingleNode = false,
      intervalMillis = 1000,
      config = application.config
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown)
    leaseElector.shutdown()
  }

  private def runNext(_context: Option[JValue]): Option[JValue] = {
    if (isWorker) {
      tiedoteService.processNext()
      tiedoteService.retryFailed()
    }

    None
  }

  private def isWorker = leaseElector.hasLease
}
