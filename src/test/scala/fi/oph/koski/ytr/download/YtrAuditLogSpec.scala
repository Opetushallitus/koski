package fi.oph.koski.ytr.download

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
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

  val hetu = "140380-336X"

  lazy val oppijaOid =
    KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(hetu).get.oid

  "Lisäys" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

    verifyAuditLogs(
      List.fill(6)(
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

    getYtrOppija(oppijaOid, MockUsers.ophkatselija)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.ophkatselija.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
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

    getYtrOppijaVersionumerolla(oppijaOid, 1, MockUsers.ophkatselija)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.ophkatselija.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> "1"
          )
        )
      )
    )
  }

  "Tallennetun originaalin katsominen" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(modifiedSince, force = true)
    AuditLogTester.clearMessages

    getYtrSavedOriginal(oppijaOid, MockUsers.ophkatselija)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.ophkatselija.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid
          )
        )
      )
    )
  }

  "Ajantasaisen originaalin katsominen" in {
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(modifiedSince, force = true)
    AuditLogTester.clearMessages

    getYtrCurrentOriginal(oppijaOid, MockUsers.ophkatselija)

    verifyAuditLogs(
      List(
        Map(
          "operation" -> KoskiOperation.YTR_OPISKELUOIKEUS_KATSOMINEN.toString,
          "user" -> Map(
            "oid" -> MockUsers.ophkatselija.oid
          ),
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid
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
        AuditLogTester.verifyAuditLogString(
          logMessage, expectedAuditLogParams(i)
        )
    }
  }
}
