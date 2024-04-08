package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

class MassaluovutusCleanupScheduler(application: KoskiApplication) extends Logging {
  val massaluovutukset: MassaluovutusService = application.massaluovutusService
  val concurrency: Int = MassaluovutusUtils.concurrency(application.config)

  def scheduler: Option[Scheduler] = {
    Some(new Scheduler(
      application.masterDatabase.db,
      "massaluovutus-cleanup",
      new IntervalSchedule(application.config.getDuration("kyselyt.cleanupInterval")),
      None,
      runNextQuery,
      intervalMillis = 1000
    ))
  }

  def trigger(): Unit = runNextQuery(None)

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances

    massaluovutukset.cleanup(instances)
    runAsWorkerIfWorkersMissing()

    None
  }

  private def runAsWorkerIfWorkersMissing(): Unit = {
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances
    if (instances.size < concurrency && !MassaluovutusUtils.isQueryWorker(application, concurrency)) {
      logger.warn("Query worker is missing. Promoting this instance to process the queue.")
      application.scheduledTasks.restartMassaluovutusScheduler()
    }

  }
}
