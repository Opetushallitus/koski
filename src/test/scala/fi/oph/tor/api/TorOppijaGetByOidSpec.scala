package fi.oph.tor.api

import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.oppija.MockOppijat
import org.scalatest.{FreeSpec, Matchers}

class TorOppijaGetByOidSpec extends FreeSpec with Matchers with HttpSpecification {
  "/api/oppija/" - {
    SharedJetty.start
    "GET" - {
      "with valid oid" in {
        get("api/oppija/" + MockOppijat.eero.oid, headers = authHeaders()) {
          verifyResponseStatus(200)
        }
      }
      "with invalid oid" in {
        get("api/oppija/blerg", headers = authHeaders()) {
          verifyResponseStatus(400)
        }
      }
    }
  }
}
