package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DB
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class MassaluovutusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "massaluovutus"
  val schedulerDb: DB = application.masterDatabase.db
  val backpressureDuration: Duration = application.config.getDuration("kyselyt.backpressureLimits.duration")
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  sys.addShutdownHook {
    massaluovutukset.cancelAllTasks("Interrupted: worker shutdown")
  }

  def scheduler: Option[Scheduler] = {
    val arn = application.ecsMetadata.taskARN
    val allInstances = application.ecsMetadata.currentlyRunningKoskiInstances
    val workerInstances = MassaluovutusUtils.workerInstances(application, concurrency)

    logger.info(s"Check eligibility for query worker: arn = ${arn.getOrElse("n/a")}, allInstances = [${allInstances.map(_.taskArn).mkString(", ")}], workerInstances = [${workerInstances.map(_.taskArn).mkString(", ")}]")

    if (isQueryWorker) {
      val workerId = application.massaluovutusService.workerId
      logger.info(s"Starting as query worker (id: $workerId)")

      Some(new Scheduler(
        schedulerDb,
        schedulerName,
        new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
        None,
        runNextQuery,
        runOnSingleNode = false,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(schedulerDb, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(schedulerDb, schedulerName)

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

  private def isQueryWorker = MassaluovutusUtils.isQueryWorker(application, concurrency)
}
