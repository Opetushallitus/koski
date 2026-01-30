package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DB
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, WorkerLeaseElector}
import org.json4s.JValue

import java.time.Duration

class MassaluovutusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "massaluovutus"
  val schedulerDb: DB = application.masterDatabase.db
  val backpressureDuration: Duration = application.config.getDuration("kyselyt.backpressureLimits.duration")
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)
  val massaluovutukset: MassaluovutusService = application.massaluovutusService
  private val leaseElector = new WorkerLeaseElector(
    application.workerLeaseRepository,
    schedulerName,
    application.instanceId,
    slots = concurrency,
    leaseDuration = application.config.getDuration("kyselyt.workerLease.duration"),
    heartbeatInterval = application.config.getDuration("kyselyt.workerLease.heartbeatInterval")
  )

  sys.addShutdownHook {
    leaseElector.shutdown()
    massaluovutukset.cancelAllTasks("Interrupted: worker shutdown")
  }

  var schedulerInstance: Option[Scheduler] = None

  def scheduler: Option[Scheduler] = {
    leaseElector.start(
      onAcquired = _ => logger.info(s"Acquired massaluovutus lease (workerId: ${application.massaluovutusService.workerId})"),
      onLost = _ => logger.warn("Lost massaluovutus lease")
    )

    schedulerInstance = Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
      None,
      runNextQuery,
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

  private def runNextQuery(_context: Option[JValue]): Option[JValue] = {
    if (isQueryWorker) {
      if (massaluovutukset.hasNext) {
        if (massaluovutukset.systemIsOverloaded) {
          logger.info(s"System is overloaded. Postponing running the next query for $backpressureDuration")
          Scheduler.pauseForDuration(schedulerDb, schedulerName, backpressureDuration)
        } else {
          massaluovutukset.runNext()
        }
      }
    }
    None // MassaluovutusScheduler päivitä kontekstia vain käynnistyessään
  }

  private def isQueryWorker = leaseElector.hasLease
}
