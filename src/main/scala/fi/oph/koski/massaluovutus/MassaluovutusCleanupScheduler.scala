package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.GlobalIntervalScheduler

class MassaluovutusCleanupScheduler(application: KoskiApplication) extends Logging {
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  def scheduler: Option[GlobalIntervalScheduler] = {
    Some(GlobalIntervalScheduler(
      application,
      "massaluovutus-cleanup",
      application.config.getDuration("kyselyt.cleanupInterval"),
      runNextQuery,
      shouldFireCheckIntervalMillis = 1000,
      concurrency = 1
    ))
  }

  def trigger(): Unit = runNextQuery()

  private def runNextQuery(): Unit = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("massaluovutus")
    massaluovutukset.cleanup(activeWorkers)
  }
}
