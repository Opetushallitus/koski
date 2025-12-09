package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class TodistusScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus"
  val schedulerDb = application.masterDatabase.db
  val todistusService: TodistusService = application.todistusService

  sys.addShutdownHook {
    todistusService.markAllMyJobsInterrupted()
  }

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    val arn = application.ecsMetadata.taskARN
    val allInstances = application.ecsMetadata.currentlyRunningKoskiInstances

    logger.info(s"Check eligibility for todistus worker: arn = ${arn.getOrElse("n/a")}, allInstances = [${allInstances.map(_.taskArn).mkString(", ")}], workerInstances = [${workerInstances.map(_.taskArn).mkString(", ")}]")

    if (isTodistusWorker) {
      val workerId = application.todistusRepository.workerId
      logger.info(s"Starting as todistus worker (id: $workerId)")

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
    } else {
      schedulerInstance = None
    }
    schedulerInstance
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(schedulerDb, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(schedulerDb, schedulerName)

  private def runNextTodistus(_context: Option[JValue]): Option[JValue] = {
    if (isTodistusWorker) {
      if (todistusService.hasNext) {
        todistusService.runNext()
      }
    }

    None
  }

  private def workerInstances: Seq[KoskiInstance] = {
    application.ecsMetadata
      .currentlyRunningKoskiInstances
      .sortBy(_.createdAt)
      .reverse
      .take(1)
  }

  private def isTodistusWorker = {
    application.ecsMetadata.taskARN.forall { myArn =>
      workerInstances.exists(_.taskArn == myArn)
    }
  }


}
