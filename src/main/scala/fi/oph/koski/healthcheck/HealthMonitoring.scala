package fi.oph.koski.healthcheck

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.json4s.jackson.JsonMethods

class HealthMonitoring extends Logging {
  val operational: collection.mutable.Map[String, Boolean] = collection.mutable.Map.empty

  def log(status: Seq[SubsystemHealthStatus]): Unit = {
    val json = JsonSerializer.writeWithRoot(status)
    logger.info(json)
  }

  def setSubsystemStatus(subsystem: String, operational: Boolean): Unit = {
    if (!this.operational.get(subsystem).contains(operational)) {
      this.operational += (subsystem -> operational)
      log(List(SubsystemHealthStatus(
        subsystem,
        operational,
        if (!operational) Some("Not responding") else None,
      )))
    }
  }
}

object Subsystem {
  val KoskiDatabase = "KoskiDatabase"
  val RaportointiDatabase = "RaportointiDatabase"
  val ValpasDatabase = "ValpasDatabase"
  val PerustiedotIndex = "PerustiedotIndex"
  val TiedonsiirtoIndex = "TiedonsiirtoIndex"
  val Oppijanumerorekisteri = "Oppijanumerorekisteri"
  val OpenSearch = "OpenSearch"
  val Koodistopalvelu = "Koodistopalvelu"
  val Organisaatiopalvelu = "Organisaatiopalvelu"
  val EPerusteet = "EPerusteet"
  val CAS = "CAS"
  val Virta = "Virta"
}

case class SubsystemHealthStatus(
  subsystem: String,
  operational: Boolean,
  message: Option[String],
)

object SubsystemHealthStatus {
  def apply(keyValue: (String, HttpStatus)): SubsystemHealthStatus =
    SubsystemHealthStatus(
      keyValue._1,
      keyValue._2.isOk,
      if (keyValue._2.isError) Some(keyValue._2.errors.map(e => JsonMethods.pretty(e.message)).mkString("\n")) else None,
    )
}
