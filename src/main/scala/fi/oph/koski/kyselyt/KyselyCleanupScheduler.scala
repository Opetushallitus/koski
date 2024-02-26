package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

class KyselyCleanupScheduler(application: KoskiApplication) extends Logging {
  val kyselyt: KyselyService = application.kyselyService
  lazy val isQueryWorker: Boolean = KyselyUtils.isQueryWorker(application)

  def scheduler: Option[Scheduler] = {
    if (isQueryWorker) {
      Some(new Scheduler(
        application.masterDatabase.db,
        "kysely-cleanup",
        new IntervalSchedule(application.config.getDuration("kyselyt.cleanupInterval")),
        None,
        runNextQuery,
        intervalMillis = 1000
      ))
    } else {
      None
    }
  }

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    kyselyt.cleanup()
    None
  }
}
