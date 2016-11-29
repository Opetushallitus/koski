package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class OppijaGetByOidSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "/api/oppija/" - {
    "GET" - {
      "with valid oid" in {
        get("api/oppija/" + MockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatus(200)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_KATSOMINEN"))
        }
      }
      "with invalid oid" in {
        get("api/oppija/blerg", headers = authHeaders()) {
          verifyResponseStatus(400)
        }
      }
      "with unknown oid" in {
        get("api/oppija/1.2.246.562.24.90000000001", headers = authHeaders()) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.90000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
        }
      }
    }
  }
}
