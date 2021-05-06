package fi.oph.koski.koskiuser

import fi.oph.koski.api.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.HttpTester
import org.scalatest.{FreeSpec, Matchers, _}


class CookieSpec extends FreeSpec with Matchers with KoskiHttpSpec with HttpTester {

  "Huollettavien tietoja ei näytetä evästeissä" in {
    val headers = Map(
      "hetu" -> KoskiSpecificMockOppijat.faija.hetu.get,
      "security" -> "mock"
    )

    get("/cas/oppija", headers = headers) {
      response.headers.get("Set-Cookie").get.mkString should not include ("huollettavat")
    }
  }
}
