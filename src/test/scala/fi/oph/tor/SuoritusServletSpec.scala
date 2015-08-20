package fi.oph.tor

import fi.oph.tor.fixture.SuoritusTestData._
import fi.oph.tor.json.Json
import fi.oph.tor.model.Identified.withoutId
import fi.oph.tor.model.Suoritus
import org.scalatest.FreeSpec
import org.scalatra.test.scalatest.ScalatraSuite

class SuoritusServletSpec extends FreeSpec with ScalatraSuite with TorTest {
  lazy val tor: TodennetunOsaamisenRekisteri = initLocalRekisteri
  lazy val suoritusServlet = new SuoritusServlet(tor)
  addServlet(suoritusServlet, "/*")

  "Yksi tutkintosuoritus tallennetaan kantaan" - {
    "POST / -> tallennus onnistuu" in {
      post("/", Json.write(tutkintosuoritus1), Map("Content-type" -> "application/json")) {
        status should equal (200)
      }
    }
    "GET / -> kaikki suoritukset" in {
      verifySuoritukset("/", List(tutkintosuoritus1))
    }
    "GET /?oppijaId=x -> henkilön suoritukset" in {
      verifySuoritukset("/?oppijaId=person1", List(tutkintosuoritus1))
      verifySuoritukset("/?oppijaId=wrongPerson", List())
    }
    "GET /?jarjestajaOrganisaatioId=x -> organisaation suoritukset" in {
      verifySuoritukset("/?jarjestajaOrganisaatioId=org1", List(tutkintosuoritus1))
      verifySuoritukset("/?jarjestajaOrganisaatioId=wrongOrg", List())
    }
    "GET /?completedAfter=x -> suoritukset annetun päivämäärän jälkeen" in {
      verifySuoritukset("/?completedAfter=2014-06-20", List(vainKomo111)) // Samana päivänä kirjatut suoritukset otetaan mukaan
      verifySuoritukset("/?completedAfter=2014-06-20T07:00Z", List(vainKomo111))
      verifySuoritukset("/?completedAfter=2014-06-20T10:00Z", List())
      verifySuoritukset("/?completedAfter=2014-06-20T10:00%2B03:00", List(vainKomo111))
      verifySuoritukset("/?completedAfter=2014-06-20T13:00%2B03:00", List())
      verifySuoritukset("/?completedAfter=2014-06-21", List())
      get("/?completedAfter=qwer") {
        status should equal(400)
      }
    }
    "GET /?asdf=qwer -> bad request" in {
      get("/?asdf=qwer") {
        status should equal(400)
      }
    }
  }

  private def verifySuoritukset(path: String, expectedSuoritukset: List[Suoritus]) = {
    get(path) {
      status should equal (200)
      response.getContentType() should equal ("application/json;charset=utf-8")
      val suoritukset: List[Suoritus] = Json.read[List[Suoritus]](body)
      suoritukset.map(_.id) should not contain None
      Json.writePretty(suoritukset.map(withoutId)) should be (Json.writePretty(expectedSuoritukset.map(withoutId)))
    }
  }
}
