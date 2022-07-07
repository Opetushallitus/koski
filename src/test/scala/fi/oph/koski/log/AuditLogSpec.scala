package fi.oph.koski.log

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.koskiuser.MockUsers
import fi.vm.sade.auditlog.Logger
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.Assertions
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.util.matching.Regex

class AuditLogSpec extends AnyFreeSpec with TestEnvironment with Assertions with Matchers {
  private val loggerMock = mock(classOf[Logger])
  private val auditLogMock = new AuditLog(loggerMock)
  private lazy val käyttöoikeuspalvelu = KoskiApplicationForTests.käyttöoikeusRepository

  verifyLogMessage("""\{"version":1,"logSeq":0,"type":"alive","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","message":"started"}""".r)

  "AuditLog" - {
    "Logs in JSON format" in {
      auditLogMock.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_LISAYS, MockUsers.omniaPalvelukäyttäjä.toKoskiSpecificSession(käyttöoikeuspalvelu), Map(KoskiAuditLogMessageField.oppijaHenkiloOid ->  "1.2.246.562.24.00000000001")))
      verifyLogMessage("""\{"version":1,"logSeq":\d+,"type":"log","bootTime":".*","hostname":"","timestamp":".*","serviceName":"koski","applicationType":"backend","user":\{"oid":"1.2.246.562.24.99999999989","ip":"192.168.0.10","session":"","userAgent":""\},"operation":"OPISKELUOIKEUS_LISAYS","target":\{"oppijaHenkiloOid":"1.2.246.562.24.00000000001"\},"changes":\[\]\}""".r)
    }

    "throws when trying to log data with more than about 15000 characters" in {
      val pitkäMerkkijono = "A" * 15000
      val message = KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_HAKU, MockUsers.omniaPalvelukäyttäjä.toKoskiSpecificSession(käyttöoikeuspalvelu), Map(KoskiAuditLogMessageField.hakuEhto -> pitkäMerkkijono))

      intercept[RuntimeException] {
        AuditLog.log(message)
      }
    }
  }

  private def verifyLogMessage(expectedMessage: Regex) {
    val infoCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    verify(loggerMock, atLeastOnce).log(infoCapture.capture)
    val logMessage: String = infoCapture.getValue
    logMessage should fullyMatch regex(expectedMessage)
  }
}
