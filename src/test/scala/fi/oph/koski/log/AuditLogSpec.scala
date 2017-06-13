package fi.oph.koski.log

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUsers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.{Assertions, FreeSpec, Matchers}
import org.slf4j.Logger

import scala.util.matching.Regex

class AuditLogSpec extends FreeSpec with Assertions with Matchers {
  val loggerMock = mock(classOf[Logger])
  val audit = new AuditLog(loggerMock)
  lazy val käyttöoikeuspalvelu = KoskiApplicationForTests.käyttöoikeusRepository

  verifyLogMessage("""\{"logSeq":"0","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","message":"Server started!"}""".r)

  "AuditLog" - {
    "Logs in JSON format" in {
      audit.log(AuditLogMessage(KoskiOperation.OPISKELUOIKEUS_LISAYS, MockUsers.omniaPalvelukäyttäjä.toKoskiUser(käyttöoikeuspalvelu), Map(KoskiMessageField.oppijaHenkiloOid ->  "1.2.246.562.24.00000000001")))
      verifyLogMessage("""\{"logSeq":"\d+","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","kayttajaHenkiloNimi":"omnia-palvelukäyttäjä käyttäjä","oppijaHenkiloOid":"1.2.246.562.24.00000000001","clientIp":"192.168.0.10","kayttajaHenkiloOid":"1.2.246.562.24.99999999989","operaatio":"OPISKELUOIKEUS_LISAYS"}""".r)
    }
  }

  private def verifyLogMessage(expectedMessage: Regex) {
    val infoCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    verify(loggerMock, atLeastOnce).info(infoCapture.capture)
    val logMessage: String = infoCapture.getValue
    logMessage should fullyMatch regex(expectedMessage)
  }
}
