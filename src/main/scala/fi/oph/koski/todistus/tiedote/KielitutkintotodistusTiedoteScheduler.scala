package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{FixedTimeOfDaySchedule, IntervalSchedule, Scheduler, WorkerLeaseElector}
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

  private def runBatch(_context: Option[JValue]): Option[JValue] = {
    if (isWorker) {
      tiedoteService.processAll()
      tiedoteService.retryAllFailed()
    }

    None
  }

  private def isWorker = leaseElector.hasLease
}
