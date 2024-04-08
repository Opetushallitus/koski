package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.tiedonsiirto.TiedonsiirtoScheduler

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Option[Scheduler] = new UpdateHenkilotTask(application).scheduler
  val syncPerustiedot: Option[Scheduler] = application.perustiedotSyncScheduler.scheduler
  val manualSyncPerustiedot: Option[Scheduler] = application.perustiedotManualSyncScheduler.scheduler
  val syncTiedonsiirrot = new TiedonsiirtoScheduler(
    application.masterDatabase.db,
    application.config,
    application.tiedonsiirtoService
  )
  val purgeOldSessions: Option[Scheduler] = new PurgeOldSessionsTask(application).scheduler
  var runQueries: Option[Scheduler] = application.massaluovutusScheduler.scheduler
  val cleanupQueries: Option[Scheduler] = application.massaluovutusCleanupScheduler.scheduler

  def init {}

  def restartMassaluovutusScheduler(): Unit = {
    runQueries.foreach { _.shutdown }
    runQueries = application.massaluovutusScheduler.scheduler
  }
}

