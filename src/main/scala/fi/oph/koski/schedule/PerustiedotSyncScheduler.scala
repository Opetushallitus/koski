package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.Timing
import org.json4s.JValue

case class PerustiedotSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Scheduler = new Scheduler(app.masterDatabase.db, "perustiedot-sync", new IntervalSchedule(app.config.getDuration("schedule.perustiedotSyncInterval")), None, syncAndLogErrors, intervalMillis = 1000)

  def syncAndLogErrors(ignore: Option[JValue]): Option[JValue] = timed("perustiedotSync") {
    try {
      sync
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running perustiedotSync")
    }
    None
  }

  def sync: Unit = synchronized {
    logger.debug("Checking for sync rows")
    val rows = app.perustiedotSyncRepository.needSyncing(1000)
    if (rows.nonEmpty) {
      logger.info(s"Syncing ${rows.length} rows")
      rows.groupBy(_.upsert) foreach { case (upsert, rows) =>
        app.perustiedotIndexer.updateBulkRaw(rows.map(_.data), upsert)
      }
      app.perustiedotSyncRepository.delete(rows.map(_.id))
      logger.info(s"Done syncing ${rows.length} rows")
    }
  }
}