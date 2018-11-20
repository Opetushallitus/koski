package fi.oph.koski.koskiuser

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import javax.servlet.http.HttpServletRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}

import collection.JavaConverters._

class LogoutServletTest extends FreeSpec with Matchers with MockFactory {

  implicit val application = KoskiApplication(ConfigFactory.parseString(
    """
      |configurable.logout.url.fi = "https://opintopolku.fi/shibboleth/Logout?return=%2Fkoski%2Fuser%2Fredirect%3Ftarget%3D"
    """.stripMargin).withFallback(defaultConfig))

  "LogoutServlet" - {
    "Palauttaa oikean logout URL:n kun target-parametri on asetettu" in {

      val parameterMap: java.util.Map[String, Array[String]] = Map("target" -> Array("https://www.hsl.fi/etusivu/#linkki")).asJava

      def logoutServlet = new LogoutServlet {
        override implicit val request: javax.servlet.http.HttpServletRequest = stub[HttpServletRequest]
        (() => request.getParameterMap) when() returning(parameterMap)
      }

      val logoutURL = logoutServlet.getLogoutUrl

      logoutURL should equal("https://opintopolku.fi/shibboleth/Logout?return=%2Fkoski%2Fuser%2Fredirect%3Ftarget%3Dhttps%253A%252F%252Fwww.hsl.fi%252Fetusivu%252F%2523linkki")
    }
  }
}
