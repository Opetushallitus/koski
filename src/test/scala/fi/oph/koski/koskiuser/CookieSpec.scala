package fi.oph.koski.koskiuser

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers


class CookieSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {

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
