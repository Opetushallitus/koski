package fi.oph.koski.valpas

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import org.scalatest.BeforeAndAfterEach

class ValpasKansalainenApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "Käyttöoikeuksien tarkastus" - {
    "Tietoja ei saa haettua ilman kirjautumista" in {
      get(getOmatTiedotUrl) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
      }
    }

    "Tiedot saa haettua kirjautuneena kansalaisena" in {
      val kansalainen = ValpasMockOppijat.lukioOpiskelija
      get(getOmatTiedotUrl, headers = kansalainenLoginHeaders(kansalainen.hetu.get)) {
        verifyResponseStatusOk()
      }
    }

    "Tietoja ei saa haettua kirjautuneena virkailijana" in {
      get(getOmatTiedotUrl, headers = authHeaders()) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }
  }

  "Audit-logit" - {
    "Kansalaisen omien tietojen hakeminen tuottaa audit-logimerkinnän" in {
      val oppija = ValpasMockOppijat.lukioOpiskelija
      get(getOmatTiedotUrl, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessage(katsominenAuditLogMessage(List(oppija.oid)))
      }
    }

    "Kansalaisen tietojen hakeminen tuottaa audit-logimerkinnän huollettavista" in {
      get(getOmatTiedotUrl, headers = kansalainenLoginHeaders("240470-621T")) {
        verifyResponseStatusOk() // Huoltajalla 240470-621T itsellään ei ole tietoja Valppaassa, joten viestissä on vain Valppaasta löytyvät huollettavat
        AuditLogTester.verifyLastAuditLogMessage(huoltajaKatsominenAuditLogMessage(List(
          ValpasMockOppijat.turvakieltoOppija.oid,
          ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
        )))
      }
    }
  }

  def getOmatTiedotUrl = "/valpas/api/kansalainen/tiedot"

  def katsominenAuditLogMessage(oppijaOidit: Seq[String]) =
    Map(
      "operation" -> ValpasOperation.VALPAS_KANSALAINEN_KATSOMINEN.toString,
      "target" -> Map(
        ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> oppijaOidit.mkString(" ")
      )
    )

  def huoltajaKatsominenAuditLogMessage(oppijaOidit: Seq[String]) =
    Map(
      "operation" -> ValpasOperation.VALPAS_KANSALAINEN_HUOLTAJA_KATSOMINEN.toString,
      "target" -> Map(
        ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> oppijaOidit.mkString(" ")
      )
    )

}
