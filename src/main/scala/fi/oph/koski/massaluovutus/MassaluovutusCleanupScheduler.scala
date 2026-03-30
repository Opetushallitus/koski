package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler, SchedulerMode}

class MassaluovutusCleanupScheduler(application: KoskiApplication) extends Logging {
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  def scheduler: Option[Scheduler] = {
    Some(Scheduler(
      application,
      "massaluovutus-cleanup",
      new IntervalSchedule(application.config.getDuration("kyselyt.cleanupInterval")),
      runNextQuery,
      intervalMillis = 1000,
      mode = SchedulerMode.leaseControlledWithSharedSchedule(concurrency = 1)
    ))
  }

  def trigger(): Unit = runNextQuery()

  private def runNextQuery(): Unit = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("massaluovutus")
    massaluovutukset.cleanup(activeWorkers)
  }
}
