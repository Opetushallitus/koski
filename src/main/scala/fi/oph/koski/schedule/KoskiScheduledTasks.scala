package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.tiedonsiirto.TiedonsiirtoScheduler

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Option[Scheduler] = new UpdateHenkilotTask(application).scheduler
  val syncPerustiedot: Option[Scheduler] = application.perustiedotSyncScheduler.scheduler
  val syncTiedonsiirrot = new TiedonsiirtoScheduler(application.masterDatabase.db, application.config, application.tiedonsiirtoService)
  def init {}
}

