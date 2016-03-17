package fi.oph.tor.toruser

import fi.oph.tor.api.LocalJettyHttpSpecification
import fi.oph.tor.json.Json
import fi.oph.tor.log.AuditLogTester
import org.scalatest.{Matchers, FreeSpec}

class AuthenticationSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  AuditLogTester.setup

  "POST /login" - {
    "Valid credentials" in {
      post("user/login", Json.write(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(200)
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "LOGIN", "kayttajaHenkiloOid" -> MockUsers.kalle.oid))
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
