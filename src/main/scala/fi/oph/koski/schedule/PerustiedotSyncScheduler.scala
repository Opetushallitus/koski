package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing
import org.json4s.JValue

case class PerustiedotSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] =
    if (app.config.getString("schedule.perustiedotSyncInterval") == "never") {
      None
    } else {
      Some(new Scheduler(
        app.masterDatabase.db,
        "perustiedot-sync",
        new IntervalSchedule(app.config.getDuration("schedule.perustiedotSyncInterval")),
        None,
        syncAndLogErrors,
        intervalMillis = 1000,
        config = app.config
      ))
    }

  private def syncAndLogErrors(ignore: Option[JValue]): Option[JValue] = timed("perustiedotSync", 500) {
    try {
      app.perustiedotIndexer.sync(refresh = true)
    } catch {
      case e: Exception => logger.error(e)("Problem running perustiedotSync")
    }
    None
  }
}
