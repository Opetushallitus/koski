package fi.oph.koski.log

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import org.scalatest.{FreeSpec, Matchers}

class LocalAuditLogServletSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {

  private lazy val oids = List(MockOppijat.eerola.oid, MockOppijat.tero.oid)

  "LocalAuditLogServlet" - {
    "Validoi oidit" in {
      postOids(List("6.6.6"), KoskiOperation.OPISKELUOIKEUS_HAKU.toString) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: 6.6.6. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
      }
    }
    "Validoi annetun logi operaation" in {
      postOids(oids, "FOOBAR") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Operaatiota FOOBAR ei löydy"))
      }
    }
    "Luo auditlogin" in {
      AuditLogTester.clearMessages
      postOids(oids, KoskiOperation.OPISKELUOIKEUS_HAKU.toString) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.tero.oid)))
      }
    }
  }

  private def postOids[A](oids: List[String], operation: String, user: UserWithPassword = MockUsers.paakayttaja)(f: => A): A = {
    post(
      "api/auditlog",
      JsonSerializer.writeWithRoot(LocalAuditLogRequest(oids, operation)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }
}
