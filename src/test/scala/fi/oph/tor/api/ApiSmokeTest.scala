package fi.oph.tor.api

import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.schema.TorOppijaExamples
import org.scalatest.{FreeSpec, Matchers}

class ApiSmokeTest extends FreeSpec with Matchers with HttpSpecification {
  "/api/oppija/" - {
    SharedJetty.start
    "GET" - {
      "with valid oid" in {
        get("api/oppija/" + new MockOppijaRepository().eero.oid, headers = authHeaders) {
          verifyResponseStatus()
        }
      }
      "with invalid oid" in {
        get("api/oppija/blerg", headers = authHeaders) {
          verifyResponseStatus(400)
        }
      }
    }

    TorOppijaExamples.examples.foreach { example =>
      "POST " + example.name in {
        val body = Json.write(example.data).getBytes("utf-8")
        put("api/oppija", body = body, headers = authHeaders ++ jsonContent) {
          verifyResponseStatus()
          println(example.name + ": OK")
        }
      }
    }
  }
}
