package fi.oph.koski.massaluovutus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.schedule.{IntervalSchedule, Scheduler}
import org.json4s.JValue

class MassaluovutusCleanupScheduler(application: KoskiApplication) extends Logging {
  val massaluovutukset: MassaluovutusService = application.massaluovutusService

  def scheduler: Option[Scheduler] = {
    Some(new Scheduler(
      application.masterDatabase.db,
      "massaluovutus-cleanup",
      new IntervalSchedule(application.config.getDuration("kyselyt.cleanupInterval")),
      None,
      runNextQuery,
      intervalMillis = 1000,
      config = application.config
    ))
  }

  def trigger(): Unit = runNextQuery(None)

  private def runNextQuery(_ignore: Option[JValue]): Option[JValue] = {
    val activeWorkers = application.workerLeaseRepository.activeHolders("massaluovutus")

    massaluovutukset.cleanup(activeWorkers)

    None
  }
}
