package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing

import java.time.Duration

case class PerustiedotManualSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] = Some(Scheduler(
    app,
    "perustiedot-manual-sync",
    new IntervalSchedule(Duration.ofMinutes(5)),
    manualSyncAndLogErrors,
    mode = LeaseControlledWithSharedSchedule(1)
  ))

  private def manualSyncAndLogErrors(): Unit = timed("perustiedotManualSync", 500) {
    try {
      app.perustiedotIndexer.manualSync(refresh = true)
    } catch {
      case e: Exception => logger.error(e)("Problem running perustiedotManualSync")
    }
  }
}
