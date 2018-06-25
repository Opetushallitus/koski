package fi.oph.koski.mydata

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import org.scalatest.{FreeSpec, Matchers}

class MyDataAPIProxyServletTest extends FreeSpec with LocalJettyHttpSpecification with Matchers with HttpTester {

  def oid = "1.2.3.4.5"
  implicit def application = KoskiApplicationForTests

  "ApiProxyServlet" - {

    "Ei palauta mitään mikäli X-ROAD-MEMBER headeria ei ole asetettu" in {
      authGet(s"api/omadata/oppija/${oid}") {
        status should equal(400)
        body should include("Vaadittu X-ROAD-MEMBER http-otsikkokenttä puuttuu")
      }
    }

    "Ei palauta mitään mikäli käyttäjä ei ole antanut lupaa" in {
      authGet(s"api/omadata/oppija/${oid}", headers = Map("X-ROAD-MEMBER" -> "2769790-1")) {
        status should equal(400)
        body should include("X-ROAD-MEMBER:llä ei ole lupaa hakea opiskelijan tietoja")
      }
    }

  }
}
