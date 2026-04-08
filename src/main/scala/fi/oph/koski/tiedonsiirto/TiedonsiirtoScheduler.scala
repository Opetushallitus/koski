package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schedule.IndependentIntervalScheduler
import fi.oph.koski.util.Timing

class TiedonsiirtoScheduler(application: KoskiApplication, tiedonsiirtoService: TiedonsiirtoService) extends Timing {
  val scheduler: IndependentIntervalScheduler =
    IndependentIntervalScheduler(
      application,
      "tiedonsiirto-sync",
      application.config.getDuration("schedule.tiedonsiirtoSyncInterval"),
      syncTiedonsiirrot,
      shouldFireCheckIntervalMillis = 1000,
      concurrency = 0
    )

  def syncTiedonsiirrot(): Unit = {
    tiedonsiirtoService.syncToOpenSearch(refresh = true)
  }
}
