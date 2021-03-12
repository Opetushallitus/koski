package fi.oph.koski.tiedonsiirto

import com.typesafe.config.Config
import fi.oph.koski.db.DB
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.util.Timing
import org.json4s.JValue

class TiedonsiirtoScheduler(db: DB, config: Config, tiedonsiirtoService: TiedonsiirtoService) extends Timing {
  val scheduler: Scheduler =
    new Scheduler(
      db,
      "tiedonsiirto-sync",
      new IntervalSchedule(config.getDuration("schedule.tiedonsiirtoSyncInterval")),
      None,
      syncTiedonsiirrot,
      runOnSingleNode = false,
      intervalMillis = 1000
    )

  def syncTiedonsiirrot(ctx: Option[JValue]): Option[JValue] = {
    tiedonsiirtoService.syncToElasticsearch(refresh = true)
    None
  }
}
