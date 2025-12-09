package fi.oph.koski.todistus

import fi.oph.koski.config.{KoskiApplication, KoskiInstance}
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.log.Logging
import org.json4s.JValue

import java.time.Duration

class TodistusCleanupScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "todistus-cleanup"
  val schedulerDb = application.masterDatabase.db
  val todistusService = application.todistusService

  def scheduler: Option[Scheduler] = {
    Some(new Scheduler(
      schedulerDb,
      schedulerName,
      new IntervalSchedule(application.config.getDuration("todistus.cleanupInterval")),
      None,
      runNext,
      runOnSingleNode = true,
      intervalMillis = 1000
    ))
  }

  def pause(duration: Duration): Boolean = Scheduler.pauseForDuration(schedulerDb, schedulerName, duration)

  def resume(): Boolean = Scheduler.resume(schedulerDb, schedulerName)

  private def runNext(_ignore: Option[JValue]): Option[JValue] = {
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances

    todistusService.cleanup(instances)
    runAsWorkerIfWorkersMissing()

    None
  }

  private def runAsWorkerIfWorkersMissing(): Unit = {
    // TODO: TOR-2400: Korjaa tämä koodaamalla tietokantataulujen tms. avulla ilman ECS-tonkimisriippuvuutta.
    // Koodi tuskin edes toimii, instances.size ei voi koskaan olla 0...
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances
    if (instances.size < 1 && !isTodistusWorker) {
      logger.warn("Todistus worker is missing. Promoting this instance to process the queue.")
      application.scheduledTasks.restartTodistusScheduler()
    }
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
