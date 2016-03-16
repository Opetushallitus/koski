package fi.oph.tor.log

import fi.oph.tor.toruser.{MockUsers, TorUser}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.{Assertions, FreeSpec, Matchers}
import org.slf4j.Logger
import scala.util.matching.Regex

class AuditLogSpec extends FreeSpec with Assertions with Matchers {
  val loggerMock = mock(classOf[Logger])
  val audit = new AuditLog(loggerMock)

  "AuditLog" - {
    "Logs in JSON format" in {
      verifyLogMessage(AuditLogMessage(TorOperation.OPISKELUOIKEUS_LISAYS, MockUsers.hiiri.asTorUser, Map(TorMessageField.oppijaHenkiloOid ->  "1.2.246.562.24.00000000001")), """\{"timestamp":".*","serviceName":"koski","applicationType":"backend","oppijaHenkiloOid":"1.2.246.562.24.00000000001","clientIp":"192.168.0.10","kayttajaHenkiloOid":"11111","operaatio":"OPISKELUOIKEUS_LISAYS"}""".r)
    }
  }

  private def verifyLogMessage(msg: AuditLogMessage, expectedMessage: Regex) {
    audit.log(msg)
    val infoCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    verify(loggerMock, times(1)).info(infoCapture.capture)
    val logMessage: String = infoCapture.getValue
    logMessage should fullyMatch regex(expectedMessage)
  }
}
