package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.schema.FullHenkilö
import org.scalatest.{FreeSpec, Matchers}

class TorOppijaSearchSpec extends FreeSpec with Matchers with HttpSpecification {
  "/api/oppija/search" - {
    SharedJetty.start
    "Returns results" in {
      get("api/oppija/search", params = List(("query" -> "eero")), headers = authHeaders()) {
        verifyResponseStatus(200)
        Json.read[List[FullHenkilö]](body).length should equal(3)
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
