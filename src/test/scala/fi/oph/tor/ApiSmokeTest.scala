package fi.oph.tor

import com.unboundid.util.Base64
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.schema.TorOppijaExamples
import org.scalatest.{Matchers, FreeSpec}
import org.scalatra.test.HttpComponentsClient

class ApiSmokeTest extends FreeSpec with Matchers with HttpComponentsClient {
  "/api/oppija/" - {
    SharedJetty.start
    "GET" - {
      get("api/oppija/" + new MockOppijaRepository().eero.oid, headers = authHeaders) {
        response.status should(equal(200))
      }
    }

    "POST" - {
      val body = Json.write(TorOppijaExamples.uusiOppijaEiSuorituksia).getBytes("utf-8")
      post("api/oppija", body = body, headers = (authHeaders + ("Content-type" -> "application/json"))) {
        response.status should(equal(200))
      }
    }
  }

  def authHeaders: Map[String, String] = {
    val auth: String = "Basic " + Base64.encode("kalle:asdf".getBytes("UTF8"))
    Map("Authorization" -> auth)
  }

  override def baseUrl = SharedJetty.baseUrl
}
