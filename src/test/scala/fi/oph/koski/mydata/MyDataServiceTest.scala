package fi.oph.koski.mydata

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession}
import fi.oph.koski.log.{AuditLogTester, LogUserContext}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class MyDataServiceTest extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  val oid = "1.2.3.4.5" // student ID
  val memberId = "hsl"

  before {
    AuditLogTester.clearMessages
  }

  after {
    AuditLogTester.clearMessages
  }

  def getSession = new KoskiSpecificSession(AuthenticationUser(oid, "", "", None), "fi", LogUserContext.toInetAddress("127.0.0.1"), "", Set())

  "MyDataService" - {

    "Käyttäjän antama hyväksyntä logitetaan audit-lokiin" in {
      KoskiApplicationForTests.mydataService.put(memberId, getSession)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> "KANSALAINEN_MYDATA_LISAYS",
        "user" -> Map("oid" -> oid),
        "target" -> Map(
          "oppijaHenkiloOid" -> oid,
          "omaDataKumppani" -> memberId
        )
      ))
    }

    "Käyttäjän poistama hyväksyntä logitetaan audit-lokiin" in {
      KoskiApplicationForTests.mydataService.delete(memberId, getSession)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> "KANSALAINEN_MYDATA_POISTO",
        "user" -> Map("oid" -> oid),
        "target" -> Map(
          "oppijaHenkiloOid" -> oid,
          "omaDataKumppani" -> memberId
        )
      ))
    }

  }
}
