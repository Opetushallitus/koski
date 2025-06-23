package fi.oph.koski.healthcheck

import fi.oph.koski.healthcheck.Subsystem.Subsystem
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.json4s.jackson.JsonMethods

class HealthMonitoring extends Logging {
  val operational: collection.mutable.Map[Subsystem, Boolean] = collection.mutable.Map.empty

  def log(status: Seq[SubsystemHealthStatus]): Unit = {
    logList(status ++ storedStatus)
  }

  def setSubsystemStatus(subsystem: Subsystem, operational: Boolean): Unit = {
    if (!this.operational.get(subsystem).contains(operational)) {
      this.operational += (subsystem -> operational)
      logList(storedStatus)
    }
  }

  private def logList(status: Seq[SubsystemHealthStatus]): Unit = {
    status.foreach(s => logger.info(JsonSerializer.writeWithRoot(s)))
  }

  private def storedStatus: Seq[SubsystemHealthStatus] = operational.map { case (key, ok) =>
    SubsystemHealthStatus(key, ok, if (!ok) Some("Not responding") else None)
  }.toList
}

object Subsystem extends Enumeration {
  type Subsystem = Value

  val KoskiDatabase,
    RaportointiDatabase,
    ValpasDatabase,
    PerustiedotIndex,
    TiedonsiirtoIndex,
    Oppijanumerorekisteri,
    OpenSearch,
    Koodistopalvelu,
    Organisaatiopalvelu,
    EPerusteet,
    CAS,
    Virta,
    Suoritusrekisteri,
    MockSystemForTests
  = Value
}

case class SubsystemHealthStatus(
  subsystem: String,
  operational: Boolean,
  message: Option[String],
)

object SubsystemHealthStatus {
  def apply(keyValue: (Subsystem, HttpStatus)): SubsystemHealthStatus =
    SubsystemHealthStatus(
      keyValue._1.toString,
      keyValue._2.isOk,
      if (keyValue._2.isError) Some(keyValue._2.errors.map(e => JsonMethods.pretty(e.message)).mkString("\n")) else None,
    )

  def apply(subsystem: Subsystem, operational: Boolean,message: Option[String]): SubsystemHealthStatus =
    SubsystemHealthStatus(subsystem.toString, operational, message)
}
