package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusGetByIdSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "/api/opiskeluoikeus/:id" - {
    "GET" - {
      "with valid id" in {
        val id = lastOpiskeluoikeus(MockOppijat.eero.oid).id.get
        AuditLogTester.clearMessages
        get("api/opiskeluoikeus/" + id, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with invalid id" in {
        get("api/opiskeluoikeus/blerg", headers = authHeaders()) {
          verifyResponseStatus(400)
        }
      }
      "with unknown id" in {
        get("api/opiskeluoikeus/0", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla id:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
    }
  }
}
