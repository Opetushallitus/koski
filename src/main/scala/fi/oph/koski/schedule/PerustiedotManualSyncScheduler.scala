package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing
import org.json4s.JValue

import java.time.Duration

case class PerustiedotManualSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] = Some(new Scheduler(
    app.masterDatabase.db,
    "perustiedot-manual-sync",
    new IntervalSchedule(Duration.ofMinutes(5)),
    None,
    manualSyncAndLogErrors
  ))

  private def manualSyncAndLogErrors(options: Option[JValue]): Option[JValue] = timed("perustiedotManualSync", 500) {
    try {
      app.perustiedotIndexer.manualSync(refresh = true)
    } catch {
      case e: Exception => logger.error(e)("Problem running perustiedotManualSync")
    }
    None
  }
}
