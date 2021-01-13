package fi.oph.koski.mydata

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.HttpTester
import fi.oph.common.koskiuser.MockUsers
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.scalatest.{FreeSpec, Matchers}

class LogoutRedirectServletTest extends FreeSpec with LocalJettyHttpSpecification with Matchers with HttpTester {


  def application = KoskiApplicationForTests

  "LogoutRedirectServlet" - {
    "Redirectaa mikäli callback URL on sallittu" in {

      val params: Map[String, String] = Map("target" -> "https://localhost/index.html?parameter=value")

      get("user/redirect", params) {
        verifyResponseStatusOk(302)
      }
    }

    "Ei redirectaa mikäli callback URL ei ole sallittu" in {

      val params: Map[String, String] = Map("target" -> "https://www.google.com/index.html")

      get("user/redirect", params) {
        verifyResponseStatusOk(400)
      }
    }
  }
}
