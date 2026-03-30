package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing

case class PerustiedotSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] =
    if (app.config.getString("schedule.perustiedotSyncInterval") == "never") {
      None
    } else {
      Some(Scheduler(
        app,
        "perustiedot-sync",
        new IntervalSchedule(app.config.getDuration("schedule.perustiedotSyncInterval")),
        syncAndLogErrors,
        intervalMillis = 1000,
        mode = LeaseControlledWithSharedSchedule(1)
      ))
    }

  private def syncAndLogErrors(): Unit = timed("perustiedotSync", 500) {
    try {
      app.perustiedotIndexer.sync(refresh = true)
    } catch {
      case e: Exception => logger.error(e)("Problem running perustiedotSync")
    }
  }
}
