package fi.oph.koski.koskiuser

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.HttpTester
import org.scalatest.{FreeSpec, Matchers, _}


class CookieSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with HttpTester {

  "Huollettavien tietoja ei näytetä evästeissä" in {
    val headers = Map(
      "hetu" -> MockOppijat.faija.hetu.get,
      "security" -> "mock"
    )

    get("/cas/oppija", headers = headers) {
      response.headers.get("Set-Cookie").get.mkString should not include ("huollettavat")
    }
  }
}
