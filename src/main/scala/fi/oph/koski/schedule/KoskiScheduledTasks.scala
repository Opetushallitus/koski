package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.tiedonsiirto.TiedonsiirtoScheduler

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Option[GlobalIntervalScheduler] = new UpdateHenkilotTask(application).scheduler
  val syncPerustiedot: Option[GlobalIntervalScheduler] = application.perustiedotSyncScheduler.scheduler
  val manualSyncPerustiedot: Option[GlobalIntervalScheduler] = application.perustiedotManualSyncScheduler.scheduler
  val syncTiedonsiirrot = new TiedonsiirtoScheduler(
    application,
    application.tiedonsiirtoService
  )
  val purgeOldSessions: Option[GlobalIntervalScheduler] = new PurgeOldSessionsTask(application).scheduler
  val runQueries: Option[IndependentIntervalScheduler] = application.massaluovutusScheduler.scheduler
  val cleanupQueries: Option[GlobalIntervalScheduler] = application.massaluovutusCleanupScheduler.scheduler

  val todistusScheduler: Option[IndependentIntervalScheduler] = application.todistusScheduler.createScheduler
  val todistusCleanupScheduler: Option[GlobalIntervalScheduler] = application.todistusCleanupScheduler.createScheduler

  val kielitutkintotodistusTiedoteScheduler: Option[GlobalIntervalScheduler] = application.kielitutkintotodistusTiedoteScheduler.createScheduler

  def init(): Unit = {}
}
