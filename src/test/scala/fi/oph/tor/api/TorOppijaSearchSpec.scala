package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class TorOppijaSearchSpec extends FreeSpec with Matchers with SearchTestMethods {
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
      search("eero") {
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPPIJA_HAKU", "hakuEhto" -> "EERO"))
      }
    }
    "When query is missing" - {
      "Returns HTTP 400" in {
        get("api/oppija/search", headers = authHeaders()) {
          verifyResponseStatus(400, TorErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
    "When query is too short" - {
      "Returns HTTP 400" in {
        get("api/oppija/search", params = List(("query" -> "aa")), headers = authHeaders()) {
          verifyResponseStatus(400, TorErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
  }
}
