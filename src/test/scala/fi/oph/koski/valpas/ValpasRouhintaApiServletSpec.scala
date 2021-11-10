package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

class ValpasRouhintaApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
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
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_ROUHINTA_HETUHAKU.toString,
          "target" -> Map(
            ValpasAuditLogMessageField.hakulause.toString -> "161004A404E, 011005A115P, 110405A6951",
            ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> "1.2.246.562.24.00000000130 1.2.246.562.24.00000000075"
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
      doKuntaQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
      }
    }

    "Hylkää, jos kysellään Ahvenanmaan kuntia" in {
      val kunta = AhvenanmaanKunnat.ahvenanmaanKunnat(0)

      doKuntaQuery(ValpasMockUsers.valpasOphPääkäyttäjä, kunta) {
        verifyResponseStatus(400,
          ValpasErrorCategory.badRequest(s"Kunta ${kunta} ei ole koodistopalvelun tuntema manner-Suomen kunta"))
      }
    }

    "Hylkää, jos kysellään kuntakoodiston muita koodeja kuin oikeita kuntia" in {
      val kunta = "199"

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
      doKuntaQuery(ValpasMockUsers.valpasHelsinki) {
        AuditLogTester.verifyAuditLogMessage(Map(
          "operation" -> ValpasOperation.VALPAS_ROUHINTA_KUNTA.toString,
          "target" -> Map(ValpasAuditLogMessageField.hakulause.toString -> "091")))
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

  private def doKuntaQuery(user: ValpasMockUser, kunta: String = "091")(f: => Unit) = {
    val kuntaQuery =
      s"""
      {
        "kunta": "${kunta}",
        "password": "hunter2",
        "lang": "fi"
      }
      """.stripMargin

    post("/valpas/api/rouhinta/kunta", body = kuntaQuery, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

}
