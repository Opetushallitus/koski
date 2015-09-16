package fi.oph.tor

import fi.oph.tor.fixture.SuoritusTestData._
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.model.Identified.withoutId
import fi.oph.tor.model.Suoritus
import org.scalatest.{FreeSpec, Matchers}
import org.scalatra.test.HttpComponentsClient

class SuoritusServletSpec extends FreeSpec with HttpComponentsClient with Matchers {
  "Yksi tutkintosuoritus tallennetaan kantaan" - {
    SharedJetty.start

    "POST /suoritus -> tallennus onnistuu" in {
      post("suoritus", Json.write(tutkintosuoritus1), Map("Content-type" -> "application/json")) {
        status should equal (200)
      }
    }
    "GET /suoritus -> kaikki suoritukset" in {
      verifySuoritukset("suoritus", List(tutkintosuoritus1))
    }
    "GET /suoritus?oppijaId=x -> henkilön suoritukset" in {
      verifySuoritukset("suoritus?oppijaId=person1", List(tutkintosuoritus1))
      verifySuoritukset("suoritus?oppijaId=wrongPerson", List())
    }
    "GET /suoritus?jarjestajaOrganisaatioId=x -> organisaation suoritukset" in {
      verifySuoritukset("suoritus?jarjestajaOrganisaatioId=org1", List(tutkintosuoritus1))
      verifySuoritukset("suoritus?jarjestajaOrganisaatioId=wrongOrg", List())
    }
    "GET /suoritus?completedAfter=x -> suoritukset annetun päivämäärän jälkeen" in {
      verifySuoritukset("suoritus?completedAfter=2014-06-20", List(vainKomo111)) // Samana päivänä kirjatut suoritukset otetaan mukaan
      verifySuoritukset("suoritus?completedAfter=2014-06-20T07:00Z", List(vainKomo111))
      verifySuoritukset("suoritus?completedAfter=2014-06-20T10:00Z", List())
      verifySuoritukset("suoritus?completedAfter=2014-06-20T10:00%2B03:00", List(vainKomo111))
      verifySuoritukset("suoritus?completedAfter=2014-06-20T13:00%2B03:00", List())
      verifySuoritukset("suoritus?completedAfter=2014-06-21", List())
      get("suoritus?completedAfter=qwer") {
        status should equal(400)
      }
    }
    "GET /suoritus?asdf=qwer -> bad request" in {
      get("suoritus?asdf=qwer") {
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

  override def baseUrl = SharedJetty.baseUrl
}
