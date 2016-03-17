package fi.oph.tor.toruser

import fi.oph.tor.api.LocalJettyHttpSpecification
import fi.oph.tor.json.Json
import org.scalatest.{Matchers, FreeSpec}

class AuthenticationSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "POST /login" - {
    "Valid credentials" in {
      post("user/login", Json.write(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(200)
      }
    }
    "Invalid credentials" in {
      post("user/login", Json.write(Login("kalle", "asdf")), headers = jsonContent) {
        verifyResponseStatus(401)
      }
    }
  }

  "GET /logout" in {
    get("user/logout") {
      verifyResponseStatus(302)
    }
  }
}
