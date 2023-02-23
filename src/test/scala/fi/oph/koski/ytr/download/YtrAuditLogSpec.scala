package fi.oph.koski.ytr.download

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.schema.Oppija
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrAuditLogSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods
    with OpiskeluoikeusTestMethods
    with BeforeAndAfterEach
{

  override protected def beforeEach() {
    super.beforeEach()
    clearYtrData()
    AuditLogTester.clearMessages
  }

  val birthmonthStart = "1980-03"
  val birthmonthEnd = "1981-10"

  val modifiedSince = LocalDate.of(2023, 1, 1)

  val oppijahetut = List(
    "080380-2432",
    "140380-336X",
    "220680-7850",
    "240680-087S"
  )

  lazy val oppijaOidEnnestäänKoskessa2 =
    KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(oppijahetut(1)).get.oid

  "Lisäys" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

    verifyAuditLogs(
      List.fill(4)(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_LISAYS.toString,
          "user" -> Map(
            "ip" -> "127.0.0.1"
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> "1"
          )
        )
      )
    )
  }

  "Muutos" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    AuditLogTester.clearMessages

    downloadYtrData(modifiedSince, force = true)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_MUUTOS.toString,
          "user" -> Map(
            "ip" -> "127.0.0.1"
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> "2"
          )
        )
      )
    )
  }

  "Katsominen" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(modifiedSince, force = true)
    AuditLogTester.clearMessages

    getYtrOppija(oppijaOidEnnestäänKoskessa2, MockUsers.paakayttaja)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.paakayttaja.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOidEnnestäänKoskessa2,
            KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> "2"
          )
        )
      )
    )
  }

  "Katsominen versionumerolla" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(modifiedSince, force = true)
    AuditLogTester.clearMessages

    getYtrOppijaVersionumerolla(oppijaOidEnnestäänKoskessa2, 1, MockUsers.paakayttaja)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.paakayttaja.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOidEnnestäänKoskessa2,
            KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> "1"
          )
        )
      )
    )
  }

  private def verifyAuditLogs(expectedAuditLogParams: List[Map[String, Object]]) = {
    val logMessages = AuditLogTester.getLogMessages
    logMessages.length should equal(expectedAuditLogParams.length)

    logMessages.zipWithIndex.map {
      case (logMessage, i) =>
        AuditLogTester.verifyAuditLogMessage(
          logMessage, expectedAuditLogParams(i)
        )
    }
  }
}
