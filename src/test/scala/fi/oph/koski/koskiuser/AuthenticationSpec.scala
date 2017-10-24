package fi.oph.koski.koskiuser

import java.lang.Thread.sleep

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class AuthenticationSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "POST /login" - {
    "Valid credentials" in {
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(200)
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "LOGIN", "kayttajaHenkiloOid" -> MockUsers.kalle.oid))
      }
    }
    "Invalid credentials" in {
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "asdf")), headers = jsonContent) {
        verifyResponseStatus(401)
      }

      // blocking because of too many login attempts
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(401)
      }

      sleep(1000)

      // blocking reset by now
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(200)
      }
    }
  }

  "GET /logout" in {
    get("user/logout") {
      verifyResponseStatus(302)
    }
  }
}
