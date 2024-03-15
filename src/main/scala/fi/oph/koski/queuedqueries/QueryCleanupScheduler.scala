package fi.oph.koski.queuedqueries

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class QueryCleanupScheduler(application: KoskiApplication) extends Logging {
  val kyselyt: QueryService = application.kyselyService

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
    kyselyt.cleanup()

    if (kyselyt.queueStalledFor(Duration.ofMinutes(1)) && !queryWorkerIsAlive) {
      logger.warn("Query worker is missing. Promoting this instance to process the queue.")
      takeover()
    }

    None
  }

  private def queryWorkerIsAlive: Boolean = {
    val context = application.kyselyScheduler.getContext
    val instances = application.ecsMetadata.currentlyRunningKoskiInstances
    context.map(_.workerId).exists(instances.contains)
  }

  private def takeover(): Unit = {
    application.kyselyScheduler.promote(true)
    application.scheduledTasks.restartKyselyScheduler()
  }
}
