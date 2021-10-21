package fi.oph.koski.valpas

import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.scalatest.BeforeAndAfterEach

class ValpasRouhintaApiServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "Hetuhaku toimii pääkäyttäjänä" in {
    doHetuQuery(ValpasMockUsers.valpasOphPääkäyttäjä) {
      verifyResponseStatusOk()
    }
  }

  "Hetuhaku toimii kuntakäyttäjänä" in {
    doHetuQuery(ValpasMockUsers.valpasHelsinki) {
      verifyResponseStatusOk()
    }
  }

  "Hetuhaku hylkää pelkillä hakeutumisenvalvonnan oikeuksilla" in {
    doHetuQuery(ValpasMockUsers.valpasHelsinkiPeruskoulu) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
    }
  }

  "Hetuhaku hylkää pelkillä suorittamisenvalvonnan oikeuksilla" in {
    doHetuQuery(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
    }
  }

  "Hetuhaku hylkää pelkillä maksuttomuudenvalvonnan oikeuksilla" in {
    doHetuQuery(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
      verifyResponseStatus(403, ValpasErrorCategory.forbidden.toiminto())
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
