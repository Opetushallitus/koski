package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter.IdHaku
import fi.oph.koski.util.Timing
import org.json4s.JValue

object PerustiedotSyncScheduler extends Timing {
  def apply(app: KoskiApplication): Scheduler =
    new Scheduler(app.masterDatabase.db, "perustiedot-sync", new IntervalSchedule(app.config.getDuration("schedule.perustiedotSyncInterval")), None, syncPerustiedot(app))

  def syncPerustiedot(app: KoskiApplication)(ignore: Option[JValue]): Option[JValue] = timed("perustiedotSync") {
    try {
      reIndex(app)
    } catch {
      case e: Exception =>
        logger.error(e)("Problem running perustiedotSync")
    }
    None
  }

  private def reIndex(app: KoskiApplication) = {
    val rows = app.perustiedotSyncRepository.get
    if (rows.nonEmpty) {
      val failed = rows.groupBy(_.opiskeluoikeusId).mapValues(_.length).filter { case (id, length) => length > 10 }
      if (failed.nonEmpty) {
        logger.error(s"Perustiedot sync failed more than 10 times for opiskeluoikeus ids ${failed.keys.mkString(", ")}")
      }
      app.perustiedotIndexer.reIndex(filters = List(IdHaku(rows.map(_.opiskeluoikeusId))))
      app.perustiedotSyncRepository.delete(rows.map(_.id))
    }
  }
}

