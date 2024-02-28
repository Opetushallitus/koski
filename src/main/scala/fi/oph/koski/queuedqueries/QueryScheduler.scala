package fi.oph.koski.queuedqueries

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue
import software.amazon.awssdk.services.rds.RdsClient

import scala.jdk.CollectionConverters._

class QueryScheduler(application: KoskiApplication) extends Logging {
  val concurrency: Int = application.config.getInt("kyselyt.concurrency")
  val kyselyt = application.kyselyService
  lazy val isQueryWorker: Boolean = QueryUtils.isQueryWorker(application)

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

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    if (kyselyt.numberOfRunningQueries < concurrency) {
      kyselyt.runNext()
    }
    None
  }
}
