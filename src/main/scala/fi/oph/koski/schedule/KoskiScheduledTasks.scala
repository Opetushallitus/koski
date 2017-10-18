package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Scheduler = new UpdateHenkilotTask(application).scheduler
  val syncPerustiedot: Scheduler = PerustiedotSyncScheduler(application)

  def init {}
}

