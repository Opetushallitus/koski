package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

class ValpasRouhintaApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, ValpasKuntarouhintaSpec.tarkastelupäivä)
  }

  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "Hetuhaku" - {

    "toimii pääkäyttäjänä" in {
      doHetuQuery(ValpasMockUsers.valpasOphPääkäyttäjä) {
        verifyResponseStatusOk()
      }
    }

    "toimii kuntakäyttäjänä" in {
      doHetuQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
      }
    }

    "hylkää pelkillä hakeutumisenvalvonnan oikeuksilla" in {
      doHetuQuery(ValpasMockUsers.valpasHelsinkiPeruskoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "hylkää pelkillä suorittamisenvalvonnan oikeuksilla" in {
      doHetuQuery(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "hylkää pelkillä maksuttomuudenvalvonnan oikeuksilla" in {
      doHetuQuery(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "jättää merkinnän auditlokiin" in {
      doHetuQuery(ValpasMockUsers.valpasHelsinki) {
        val logMessages = AuditLogTester.getLogMessages
        logMessages.length should equal(2)

        AuditLogTester.verifyAuditLogMessage(logMessages(0), Map(
          "operation" -> ValpasOperation.VALPAS_ROUHINTA_HETUHAKU.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.hakulause.toString -> "161004A404E, 011005A115P, 110405A6951",
            ValpasAuditLogMessageField.sivu.toString -> "1",
            ValpasAuditLogMessageField.sivuLukumäärä.toString -> "2",

          ),
        ))
        AuditLogTester.verifyAuditLogMessage(logMessages(1), Map(
          "operation" -> ValpasOperation.VALPAS_ROUHINTA_HETUHAKU.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> "1.2.246.562.24.00000000130 1.2.246.562.24.00000000075",
            ValpasAuditLogMessageField.sivu.toString -> "2",
            ValpasAuditLogMessageField.sivuLukumäärä.toString -> "2",
          ),
        ))

      }
    }

  }

  "Kuntarouhinta" - {

    "toimii pääkäyttäjänä" in {
      doKuntaQuery(ValpasMockUsers.valpasOphPääkäyttäjä) {
        verifyResponseStatusOk()
      }
    }

    "toimii kuntakäyttäjänä" in {
      doKuntaQuery(ValpasMockUsers.valpasHelsinki, MockOrganisaatiot.helsinginKaupunki) {
        verifyResponseStatusOk()
      }
    }

    "Hylkää, jos kysellään Ahvenanmaan kuntia" in {
      val kunta = MockOrganisaatiot.maarianhamina

      doKuntaQuery(ValpasMockUsers.valpasOphPääkäyttäjä, kunta) {
        verifyResponseStatus(400,
          ValpasErrorCategory.badRequest(s"Kunta ${kunta} ei ole koodistopalvelun tuntema manner-Suomen kunta"))
      }
    }

    "Hylkää, jos kysellään kuntakoodiston muita koodeja kuin oikeita kuntia" in {
      val kunta = MockOrganisaatiot.evira

      doKuntaQuery(ValpasMockUsers.valpasOphPääkäyttäjä, kunta) {
        verifyResponseStatus(400,
          ValpasErrorCategory.badRequest(s"Kunta ${kunta} ei ole koodistopalvelun tuntema manner-Suomen kunta"))
      }
    }

    "hylkää, jos kuntakäyttäjällä ei oikeuksia kysyttyyn kuntaan" in {
      doKuntaQuery(ValpasMockUsers.valpasTornio) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "hylkää pelkillä hakeutumisenvalvonnan oikeuksilla" in {
      doKuntaQuery(ValpasMockUsers.valpasHelsinkiPeruskoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "hylkää pelkillä suorittamisenvalvonnan oikeuksilla" in {
      doKuntaQuery(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "hylkää pelkillä maksuttomuudenvalvonnan oikeuksilla" in {
      doKuntaQuery(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
      }
    }

    "jättää merkinnän auditlokiin" in {
      doKuntaQuery(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu, ValpasKuntarouhintaSpec.kuntaOid) {
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_ROUHINTA_KUNTA.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.hakulause.toString -> ValpasKuntarouhintaSpec.kuntakoodi,
            ValpasAuditLogMessageField.oppijaHenkilöOidList.toString ->
              ValpasKuntarouhintaSpec.eiOppivelvollisuuttaSuorittavatOppijat(t)
                .map(_.oppija.oid)
                .mkString(" "),
            ValpasAuditLogMessageField.sivu.toString -> "1",
            ValpasAuditLogMessageField.sivuLukumäärä.toString -> "1",
          ),
        ))
      }
    }

  }

  private def doHetuQuery(user: ValpasMockUser)(f: => Unit) = {
    val hetuQuery =
      """
      {
        "hetut": ["161004A404E", "011005A115P", "110405A6951"],
        "password": "hunter2",
        "lang": "fi"
      }
      """.stripMargin

    post("/valpas/api/rouhinta/hetut", body = hetuQuery, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private def doKuntaQuery(user: ValpasMockUser, kuntaOid: String = ValpasKuntarouhintaSpec.kuntaOid)(f: => Unit) = {
    val kuntaQuery =
      s"""
      {
        "kuntaOid": "${kuntaOid}",
        "password": "hunter2",
        "lang": "fi"
      }
      """.stripMargin

    post("/valpas/api/rouhinta/kunta", body = kuntaQuery, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private lazy val t = new LocalizationReader(KoskiApplicationForTests.valpasLocalizationRepository, "fi")
}
