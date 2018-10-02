package fi.oph.koski.mydata

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.HttpTester
import org.scalatest.{FreeSpec, Matchers}

class MyDataAPIProxyServletTest extends FreeSpec with LocalJettyHttpSpecification with Matchers with HttpTester {

  val oid = "1.2.246.562.24.00000000023"
  val memberId = "hsl"
  val memberCode = "2769790-1" // HSL

  def application = KoskiApplicationForTests

  "ApiProxyServlet" - {
    "Ei palauta mitään mikäli X-ROAD-MEMBER headeria ei ole asetettu" in {
      authGet(s"api/omadata/oppija/${oid}") {
        status should equal(400)
        body should include("Vaadittu X-ROAD-MEMBER http-otsikkokenttä puuttuu")
      }
    }

    "Ei palauta mitään mikäli käyttäjä ei ole antanut lupaa" in {
      KoskiApplicationForTests.mydataRepository.delete(oid, memberId)

      authGet(s"api/omadata/oppija/${oid}", headers = Map("X-ROAD-MEMBER" -> memberCode)) {
        status should equal(403)
        body should include("X-ROAD-MEMBER:llä ei ole lupaa hakea opiskelijan tietoja")
      }
    }

    "Palauttaa opiskelutiedot mikäli käyttäjä on antanut siihen luvan" in {
      KoskiApplicationForTests.mydataRepository.create(oid, memberId)

      authGet(s"api/omadata/oppija/${oid}", headers = Map("X-ROAD-MEMBER" -> memberCode)) {
        status should equal(200)
        /* Cannot verify response body, it will throw "java.io.IOException: write beyond end of stream"
           when servletContext.getRequestDispatcher().forward() is called within tests
         */
      }
    }

  }
}
