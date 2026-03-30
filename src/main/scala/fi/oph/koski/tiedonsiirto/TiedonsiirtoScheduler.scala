package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import fi.oph.koski.util.Timing

class TiedonsiirtoScheduler(application: KoskiApplication, tiedonsiirtoService: TiedonsiirtoService) extends Timing {
  val scheduler: Scheduler =
    Scheduler(
      application,
      "tiedonsiirto-sync",
      new IntervalSchedule(application.config.getDuration("schedule.tiedonsiirtoSyncInterval")),
      syncTiedonsiirrot,
      intervalMillis = 1000
    )

  def syncTiedonsiirrot(): Unit = {
    tiedonsiirtoService.syncToOpenSearch(refresh = true)
  }
}
