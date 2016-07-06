package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class OppijaSearchSpec extends FreeSpec with Matchers with SearchTestMethods with LocalJettyHttpSpecification {
  AuditLogTester.setup

  "/api/oppija/search" - {
    SharedJetty.start
    "Finds by name" in {
      searchForNames("eero") should equal(List("Jouni Eerola", "Eero Esimerkki", "Eero Markkanen"))
    }
    "Finds by hetu" in {
      searchForNames("010101-123N") should equal(List("Eero Esimerkki"))
    }
    "Audit logging" in {
      search("eero", defaultUser) {
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPPIJA_HAKU", "hakuEhto" -> "EERO"))
      }
    }
    "When query is missing" - {
      "Returns HTTP 400" in {
        get("api/oppija/search", headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
    "When query is too short" - {
      "Returns HTTP 400" in {
        get("api/oppija/search", params = List(("query" -> "aa")), headers = authHeaders()) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
  }
}
