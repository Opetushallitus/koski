package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
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
          "target" -> Map(ValpasAuditLogMessageField.hakulause.toString -> "161004A404E")))
      }
    }

  }

  private def doHetuQuery(user: ValpasMockUser)(f: => Unit) = {
    val hetuQuery =
      """
      {
        "hetut": ["161004A404E"],
        "password": "hunter2",
        "lang": "fi"
      }
      """.stripMargin

    post("/valpas/api/rouhinta/hetut", body = hetuQuery, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }
}
