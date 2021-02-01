package fi.oph.koski.mydata

import fi.oph.common.log.LogUserContext
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSession}
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec, Matchers}

class MyDataServiceTest extends FreeSpec with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  val oid = "1.2.3.4.5" // student ID
  val memberId = "hsl"

  before {
    AuditLogTester.setup
    AuditLogTester.clearMessages
  }

  after {
    AuditLogTester.clearMessages
  }

  def getSession = new KoskiSession(AuthenticationUser(oid, "", "", None), "fi", LogUserContext.toInetAddress("127.0.0.1"), "", Set())

  "MyDataService" - {

    "Käyttäjän antama hyväksyntä logitetaan audit-lokiin" in {
      KoskiApplicationForTests.mydataService.put(memberId, getSession)

      AuditLogTester.verifyAuditLogMessage(Map(
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

      AuditLogTester.verifyAuditLogMessage(Map(
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
