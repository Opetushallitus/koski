package fi.oph.tor.integrationtest

import fi.oph.tor.http.{BasicAuthentication, HttpSpecification}
import fi.oph.tor.json.Json
import fi.oph.tor.schema.{TaydellisetHenkilötiedot, TorOppija}
import org.scalatest.{FreeSpec, Matchers, Tag}


object TorDevEnvironment extends Tag("tordev")

class OppijaIntegrationTest extends FreeSpec with Matchers with TordevHttpSpecification {
  val testOid = "1.2.246.562.24.51633620848"

  "Oppijan henkilötiedot, kansalaisuus ja äidinkieli" taggedAs(TorDevEnvironment) in {
    get("api/oppija/" + testOid, headers = authHeaders) {
      verifyResponseStatus(200)
      val oppija = Json.read[TorOppija](response.body)
      val henkilö = oppija.henkilö.asInstanceOf[TaydellisetHenkilötiedot]
      henkilö.oid should equal(testOid)
      (henkilö.kansalaisuus.get)(0).koodiarvo should equal("246")
      henkilö.äidinkieli.get.koodiarvo should equal("FI")
    }
  }
}

trait TordevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("System property " + name + " missing"))

  override def baseUrl = "http://tordev.tor.oph.reaktor.fi/tor"

  lazy val username = requiredEnv("TOR_USER")
  lazy val password = requiredEnv("TOR_PASS")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))
}