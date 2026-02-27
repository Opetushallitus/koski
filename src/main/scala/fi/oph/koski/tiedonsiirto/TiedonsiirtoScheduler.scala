package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.util.Timing
import org.json4s.JValue

class TiedonsiirtoScheduler(application: KoskiApplication, tiedonsiirtoService: TiedonsiirtoService) extends Timing {
  val scheduler: Scheduler =
    new Scheduler(
      application,
      "tiedonsiirto-sync",
      new IntervalSchedule(application.config.getDuration("schedule.tiedonsiirtoSyncInterval")),
      None,
      syncTiedonsiirrot,
      intervalMillis = 1000
    )

  def syncTiedonsiirrot(ctx: Option[JValue]): Option[JValue] = {
    tiedonsiirtoService.syncToOpenSearch(refresh = true)
    None
  }
}
