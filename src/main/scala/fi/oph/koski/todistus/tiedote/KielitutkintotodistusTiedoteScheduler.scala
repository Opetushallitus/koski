package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Schedule, Scheduler, SchedulerMode}

class KielitutkintotodistusTiedoteScheduler(application: KoskiApplication) extends Logging {
  val schedulerName = "kielitutkintotodistus-tiedote"
  val tiedoteService: KielitutkintotodistusTiedoteService = application.kielitutkintotodistusTiedoteService

  var schedulerInstance: Option[Scheduler] = None

  def createScheduler: Option[Scheduler] = {
    if (!application.config.getBoolean("tiedote.enabled")) return None

    val schedule: Schedule = new IntervalSchedule(application.config.getDuration("tiedote.checkInterval"))

    schedulerInstance = Some(Scheduler(
      application,
      schedulerName,
      schedule,
      runBatch,
      intervalMillis = 1000,
      mode = SchedulerMode.leaseControlledWithSharedSchedule(concurrency = 1)
    ))
    schedulerInstance
  }

  def shutdown(): Unit = {
    schedulerInstance.foreach(_.shutdown())
  }

  private def runBatch(): Unit = {
    tiedoteService.retryAllFailed()
    tiedoteService.processAll()
  }
}
