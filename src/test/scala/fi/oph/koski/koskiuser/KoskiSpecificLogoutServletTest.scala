package fi.oph.koski.koskiuser

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.sso.SSOConfigurationOverride
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URLEncoder

class KoskiSpecificLogoutServletTest
  extends AnyFreeSpec
    with Matchers
    with MockFactory
    with KoskiHttpSpec
    with BeforeAndAfterEach {

  override protected def afterEach(): Unit = {
    LogoutServerConfiguration.clearOverrides
    SSOConfigurationOverride.clearOverrides
  }

  "LogoutServlet kun shibboleth urlit on asetettu" - {
    "Ohjaa oikeaan logout URL:iin kun target-parametri ei ole asetettu" in {
      enableCasUrls()
      authGet(s"user/logout") {
        status shouldBe (302)
        header("Location") shouldEqual (s"https://opintopolku.fi/cas-oppija/logout?service=${baseUrl}")
      }
    }

    "Ohjaa oikeaan logout URL:iin kun target-parametri on asetettu" in {
      val target = "https://www.hsl.fi/etusivu/#linkki"
      enableCasUrls()
      authGet(s"user/logout?target=${URLEncoder.encode(target, "UTF-8")}") {
        status shouldBe (302)
        header("Location") shouldEqual ("https://opintopolku.fi/cas-oppija/logout?service=https://www.hsl.fi/etusivu/#linkki")
      }
    }
  }

  "LogoutServlet kun shibboleth urlit ei ole asetettu" - {
    "Ohjaa oikeaan logout URL:iin kun target-parametri ei ole asetettu" in {
      authGet(s"user/logout") {
        status shouldBe (302)
        header("Location") shouldEqual (s"${baseUrl}")
      }
    }

    "Ohjaa oikeaan logout URL:iin kun target-parametri on asetettu" in {
      val target = "https://www.hsl.fi/etusivu/#linkki"
      authGet(s"user/logout?target=${URLEncoder.encode(target, "UTF-8")}") {
        status shouldBe (302)
        header("Location") shouldEqual (s"https://www.hsl.fi/etusivu/#linkki")
      }
    }
  }

  def enableCasUrls(): Unit = {
    LogoutServerConfiguration.overrideKey("logout.url.fi", "https://opintopolku.fi/cas-oppija/logout?service=")
    LogoutServerConfiguration.overrideKey("configurable.logout.url.fi", "https://opintopolku.fi/cas-oppija/logout?service=")
    LogoutServerConfiguration.overrideKey("opintopolku.virkailija.url", "https://virkailija.testiopintopolku.fi")
    SSOConfigurationOverride.overrideKey("opintopolku.oppija.url", "https://opintopolku.fi")
    SSOConfigurationOverride.overrideKey("login.security", "cas")
    //config.getString("opintopolku.virkailija.url") !=
    // opintopolku.oppija.url="mock"
  }
}
