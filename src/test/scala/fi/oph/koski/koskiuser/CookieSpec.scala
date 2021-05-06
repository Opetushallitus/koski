package fi.oph.koski.koskiuser

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.{FreeSpec, Matchers}


class CookieSpec extends FreeSpec with Matchers with KoskiHttpSpec {

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
