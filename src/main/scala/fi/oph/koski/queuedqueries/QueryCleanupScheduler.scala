package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

class QueryCleanupScheduler(application: KoskiApplication) extends Logging {
  val kyselyt: QueryService = application.kyselyService
  val concurrency: Int = QueryUtils.concurrency(application.config)

  def scheduler: Option[Scheduler] = {
    Some(new Scheduler(
      application.masterDatabase.db,
      "kysely-cleanup",
      new IntervalSchedule(application.config.getDuration("kyselyt.cleanupInterval")),
      None,
      runNextQuery,
      intervalMillis = 1000
    ))
  }

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances

    kyselyt.cleanup(instances)
    runAsWorkerIfWorkersMissing()

    None
  }

  private def runAsWorkerIfWorkersMissing(): Unit = {
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances
    if (instances.size < concurrency && !QueryUtils.isQueryWorker(application, concurrency)) {
      logger.warn("Query worker is missing. Promoting this instance to process the queue.")
      application.scheduledTasks.restartKyselyScheduler()
    }

  }
}
