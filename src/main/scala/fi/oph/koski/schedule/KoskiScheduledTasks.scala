package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.tiedonsiirto.TiedonsiirtoScheduler

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Scheduler = new UpdateHenkilotTask(application).scheduler
  val syncPerustiedot: Scheduler = PerustiedotSyncScheduler(application)
  val syncTiedonsiirrot = new TiedonsiirtoScheduler(application.masterDatabase.db, application.config, application.koskiElasticSearchIndex)

  def init {}
}

