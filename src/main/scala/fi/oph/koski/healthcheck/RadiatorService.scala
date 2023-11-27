package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging

import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RadiatorService(application: KoskiApplication) extends Logging {
  private val healthCheck: HealthCheck = application.healthCheck
  private val monitoring: HealthMonitoring = application.healthMonitoring

  def getHealth: HealthDataResult = {
    val results = healthCheck.checkSystems(healthCheck.internalSystems ++ healthCheck.externalSystems)
    val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val instance = InetAddress.getLocalHost.getHostName

    val checks = results.map { case (system, status) =>
      HealthDataEntry(
        timestamp = now,
        instance = instance,
        subsystem = system,
        operational = status.isOk,
        external = healthCheck.externalSystems.contains(system),
        message = None,
      )
    }.toList

    val monitoringStatus = monitoring.operational.map { status =>
      HealthDataEntry(
        timestamp = now,
        instance = instance,
        subsystem = status._1,
        operational = status._2,
        external = false,
        message = None,
      )
    }

    HealthDataResult(checks ++ monitoringStatus)
  }
}

case class HealthDataResult(
  entries: List[HealthDataEntry],
)

case class HealthDataEntry(
  timestamp: String,
  instance: String,
  subsystem: String,
  operational: Boolean,
  external: Boolean,
  message: Option[String],
)
