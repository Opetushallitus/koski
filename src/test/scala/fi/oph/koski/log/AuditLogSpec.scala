package fi.oph.koski.log

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUsers
import fi.vm.sade.auditlog.Logger
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.{Assertions, FreeSpec, Matchers}

import scala.util.matching.Regex

class AuditLogSpec extends FreeSpec with Assertions with Matchers {
  val loggerMock = mock(classOf[Logger])
  val audit = new AuditLog(loggerMock)
  lazy val käyttöoikeuspalvelu = KoskiApplicationForTests.käyttöoikeusRepository

  verifyLogMessage("""\{"version":1,"logSeq":0,"type":"alive","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","message":"started"}""".r)

  "AuditLog" - {
    "Logs in JSON format" in {
      audit.log(AuditLogMessage(KoskiOperation.OPISKELUOIKEUS_LISAYS, MockUsers.omniaPalvelukäyttäjä.toKoskiUser(käyttöoikeuspalvelu), Map(KoskiMessageField.oppijaHenkiloOid ->  "1.2.246.562.24.00000000001")))
      verifyLogMessage("""\{"version":1,"logSeq":\d+,"type":"log","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","user":\{"oid":"1.2.246.562.24.99999999989","ip":"192.168.0.10","session":"","userAgent":""\},"operation":"OPISKELUOIKEUS_LISAYS","target":\{"oppijaHenkiloOid":"1.2.246.562.24.00000000001"\},"changes":\{\}\}""".r)
    }
  }

  private def verifyLogMessage(expectedMessage: Regex) {
    val infoCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    verify(loggerMock, atLeastOnce).log(infoCapture.capture)
    val logMessage: String = infoCapture.getValue
    logMessage should fullyMatch regex(expectedMessage)
  }
}
