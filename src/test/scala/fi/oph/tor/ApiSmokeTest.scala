package fi.oph.tor

import com.unboundid.util.Base64
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.oppija.MockOppijaRepository
import fi.oph.tor.schema.TorOppijaExamples
import org.scalatest.{FreeSpec, Matchers}
import org.scalatra.test.HttpComponentsClient

class ApiSmokeTest extends FreeSpec with Matchers with HttpComponentsClient {
  "/api/oppija/" - {
    SharedJetty.start
    "GET" - {
      get("api/oppija/" + new MockOppijaRepository().eero.oid, headers = authHeaders) {
        verifyResponseStatus()
      }
    }

    "POST" - {
      val body = Json.write(TorOppijaExamples.uusiOppijaEiSuorituksia).getBytes("utf-8")
      post("api/oppija", body = body, headers = (authHeaders + ("Content-type" -> "application/json"))) {
        verifyResponseStatus()
      }
    }
  }

  def verifyResponseStatus(status: Int = 200): Unit = {
    if (response.status != status) {
      fail("Expected status 200, got " + response.status + ", " + response.body)
    }
  }

  def authHeaders: Map[String, String] = {
    val auth: String = "Basic " + Base64.encode("kalle:asdf".getBytes("UTF8"))
    Map("Authorization" -> auth)
  }

  override def baseUrl = SharedJetty.baseUrl
}
