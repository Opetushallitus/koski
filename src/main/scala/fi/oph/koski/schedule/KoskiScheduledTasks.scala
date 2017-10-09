package fi.oph.koski.schedule

import fi.oph.koski.config.KoskiApplication

class KoskiScheduledTasks(application: KoskiApplication) {
  val updateHenkilötScheduler: Scheduler = new UpdateHenkilotTask(application).scheduler

  def init {}
}