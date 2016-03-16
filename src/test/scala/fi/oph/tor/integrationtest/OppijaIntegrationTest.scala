package fi.oph.tor.integrationtest

import fi.oph.tor.http.{BasicAuthentication, HttpSpecification}
import fi.oph.tor.oppija.MockOppijat
import org.scalatest.{FreeSpec, Matchers, Tag}


object TorDevEnvironment extends Tag("tordev")

class OppijaIntegrationTest extends FreeSpec with Matchers with TordevHttpSpecification {
  "Oppijan henkilötiedot" taggedAs(TorDevEnvironment) in {
    get("api/oppija/" + MockOppijat.eero.oid, headers = authHeaders) {
      verifyResponseStatus(200)
    }
  }
}

trait TordevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("System property " + name + " missing"))

  override def baseUrl = "http://tordev.tor.oph.reaktor.fi/tor"

  val username = requiredEnv("integrationtest_username")
  val password = requiredEnv("integrationtest_password")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))
}