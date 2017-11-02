package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.perustiedot.OpiskeluoikeudenOsittaisetTiedot
import fi.oph.koski.util.Timing
import org.json4s.JValue

case class PerustiedotSyncScheduler(app: KoskiApplication) extends Timing {
  def scheduler: Scheduler =
    new Scheduler(app.masterDatabase.db, "perustiedot-sync", new IntervalSchedule(app.config.getDuration("schedule.perustiedotSyncInterval")), None, syncPerustiedot, intervalMillis = 1000)

  def syncPerustiedot(ignore: Option[JValue]): Option[JValue] = timed("perustiedotSync") {
    try {
      reIndex
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running perustiedotSync")
    }
    None
  }

  private def reIndex: Unit = {
    logger.debug("Checking for sync rows")
    val rows = app.perustiedotSyncRepository.needSyncing(1000)
    if (rows.nonEmpty) {
      logger.info(s"Syncing ${rows.length} rows")
      val rowsMapped = rows.groupBy(_.upsert) foreach { case (upsert, rows) =>
        app.perustiedotIndexer.updateBulk(rows.map(row => extract[OpiskeluoikeudenOsittaisetTiedot](row.data)), upsert)
      }
      app.perustiedotSyncRepository.delete(rows.map(_.id))
      logger.info(s"Done syncing ${rows.length} rows")
    }
  }
}