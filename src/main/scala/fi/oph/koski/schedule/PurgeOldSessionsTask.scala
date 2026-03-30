package fi.oph.koski.schedule

import java.time.{Duration, ZonedDateTime}

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing

class PurgeOldSessionsTask(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] = Some(Scheduler(
    app,
    "purge-old-sessions",
    new IntervalSchedule(Duration.ofHours(3)),
    tryRun,
    mode = LeaseControlledWithSharedSchedule(1)
  ))

  private def tryRun(): Unit = timed("purgeOldSessions") {
    try {
      run()
    } catch {
      case e: Exception =>
        logger.error(e)("Purging old sessions failed")
    }
  }

  private def run(): Unit = {
    val purgeBefore = ZonedDateTime.now().minusYears(1).toInstant
    app.koskiSessionRepository.purgeOldSessions(purgeBefore)
  }
}
