package fi.oph.koski.koskiuser

import java.net.URLEncoder

import fi.oph.koski.api.LocalJettyHttpSpecification
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FreeSpec, Matchers}

class LogoutServletTest extends FreeSpec with Matchers with MockFactory with LocalJettyHttpSpecification with BeforeAndAfterEach {

  override def afterEach = LogoutServerConfiguration.clearOverrides

  "LogoutServlet kun shibboleth urlit on asetettu" - {
    "Ohjaa oikeaan logout URL:iin kun target-parametri ei ole asetettu" in {
      enableCasUrls()
      // config.getString("opintopolku.virkailija.url") != "mock"
      authGet(s"user/logout") {
        status shouldBe (302)
        header("Location") shouldEqual ("https://opintopolku.fi/shibboleth/Logout?return=%2Fkoski")
      }
    }

    "Ohjaa oikeaan logout URL:iin kun target-parametri on asetettu" in {
      val target = "https://www.hsl.fi/etusivu/#linkki"
      enableCasUrls()
      authGet(s"user/logout?target=${URLEncoder.encode(target, "UTF-8")}") {
        status shouldBe (302)
        header("Location") shouldEqual ("https://opintopolku.fi/cas-oppija/logout?service=https%3A%2F%2Fwww.hsl.fi%2Fetusivu%2F%23linkki")
      }
    }
  }

  "LogoutServlet kun shibboleth urlit ei ole asetettu" - {
    "Ohjaa oikeaan logout URL:iin kun target-parametri ei ole asetettu" in {
      authGet(s"user/logout") {
        status shouldBe (302)
        header("Location") shouldEqual (s"${baseUrl}/login")
      }
    }

    "Ohjaa oikeaan logout URL:iin kun target-parametri on asetettu" in {
      val target = "https://www.hsl.fi/etusivu/#linkki"
      authGet(s"user/logout?target=${URLEncoder.encode(target, "UTF-8")}") {
        status shouldBe (302)
        header("Location") shouldEqual (s"https%3A%2F%2Fwww.hsl.fi%2Fetusivu%2F%23linkki")
      }
    }
  }

  def enableCasUrls(): Unit = {
    LogoutServerConfiguration.overrideKey("logout.url.fi", "https://opintopolku.fi/cas-oppija/logout?service=")
    LogoutServerConfiguration.overrideKey("configurable.logout.url.fi", "https://opintopolku.fi/cas-oppija/logout?service=")
    //config.getString("opintopolku.virkailija.url") !=
    // opintopolku.oppija.url="mock"
  }
}
