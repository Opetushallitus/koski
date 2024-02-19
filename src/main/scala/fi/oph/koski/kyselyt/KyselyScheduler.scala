package fi.oph.koski.kyselyt

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

import java.time.Duration

class KyselyScheduler(application: KoskiApplication) extends Logging {
  val concurrency: Int = application.config.getInt("kyselyt.concurrency")
  val kyselyt = application.kyselyService

  sys.addShutdownHook {
    kyselyt.cancelAllTasks("Interrupted: worker shutdown")
  }

  def scheduler: Option[Scheduler] = {
    if (isQueryWorker) {
      Some(new Scheduler(
        application.masterDatabase.db,
        "kysely",
        new IntervalSchedule(application.config.getDuration("kyselyt.checkInterval")),
        None,
        runNextQuery,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }

  lazy val isQueryWorker: Boolean = {
    val az = application.ecsMetadata.availabilityZone
    logger.info(s"Instance availability zone: $az")
    az.isEmpty || az.contains("TODO: tietokannan az")
  }

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    if (kyselyt.numberOfRunningQueries < concurrency) {
      kyselyt.runNext()
    }
    None
  }
}
