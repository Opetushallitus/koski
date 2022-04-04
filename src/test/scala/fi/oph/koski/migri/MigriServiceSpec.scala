package fi.oph.koski.migri

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatra.auth.strategy.BasicAuthStrategy.BasicAuthRequest
import org.mockito.Mockito
import com.typesafe.config.ConfigValueFactory.fromAnyRef

import javax.servlet.http.HttpServletRequest

// Tämä ei pyöritä tällä hetkellä yhtäkään oikeata testiä.
// Tätä voi kuitenkin käyttää dokumentaationa ja nopeampaan
// manuaaliseen testaamiseen. Voit käyttää esimerkiksi
// testiopintopolussa olevaa migri-käyttäjää asettamalla
// tämän käyttäjätunnuksen ja salasanan username- ja
// password -parametreihin.
class MigriServiceSpec extends AnyFreeSpec {
  val application = KoskiApplication(defaultConfig.withValue(
    "opintopolku.virkailija.url", fromAnyRef("https://virkailija.testiopintopolku.fi")
  ))
  val migriService = new RemoteMigriService()(application)

  def basicAuthRequest: BasicAuthRequest = {
    val request = new MockAuthRequest(Mockito.mock(classOf[HttpServletRequest]))
    request
  }

  /*"MigriService" in {
    migriService.get("1.2.246.562.24.51986460849", basicAuthRequest).right.get
  }*/

  class MockAuthRequest(r: HttpServletRequest) extends BasicAuthRequest(r) {
    override def username: String = "Lasse"
    override def password: String = "Lasse"
  }
}
