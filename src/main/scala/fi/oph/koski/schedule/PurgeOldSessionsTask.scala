package fi.oph.koski.schedule

import java.time.{Duration, ZonedDateTime}

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing
import org.json4s.JValue

class PurgeOldSessionsTask(app: KoskiApplication) extends Timing {
  def scheduler: Option[Scheduler] = Some(new Scheduler(
    app.masterDatabase.db,
    "purge-old-sessions",
    new IntervalSchedule(Duration.ofHours(3)),
    None,
    tryRun,
    config = app.config
  ))

  private def tryRun(unused: Option[JValue]) = timed("purgeOldSessions") {
    try {
      run()
    } catch {
      case e: Exception =>
        logger.error(e)("Purging old sessions failed")
    }
    None
  }

  private def run(): Unit = {
    val purgeBefore = ZonedDateTime.now().minusYears(1).toInstant
    app.koskiSessionRepository.purgeOldSessions(purgeBefore)
  }
}
