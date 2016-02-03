package fi.oph.tor.api

import java.time.LocalDate

import fi.oph.tor.json.Json
import fi.oph.tor.schema.{NewHenkilö, TorOppija}
import org.scalatest.{FunSpec, Matchers}

class TorOppijaQuerySpec extends FunSpec with OpiskeluOikeusTestMethods with Matchers {
  val teija = NewHenkilö("150995-914X", "Teija", "Teija", "Tekijä")

  describe("Kyselyrajapinta") {
    describe("Kun haku osuu") {
      it("palautetaan hakutulokset") {
        putOpiskeluOikeus(Map("päättymispäivä"-> "2016-01-09")) {
          putOpiskeluOikeus(Map("päättymispäivä"-> "2013-01-09"), teija) {
            authGet ("api/oppija?opiskeluoikeusPäättynytViimeistään=2016-12-31&opiskeluoikeusPäättynytAikaisintaan=2016-01-01") {
              verifyResponseStatus(200)
              val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
              oppijat.length should equal(1)
              oppijat(0).opiskeluoikeudet(0).päättymispäivä should equal(Some(LocalDate.parse("2016-01-09")))
            }
          }
        }
      }
    }

    describe("Kun haku ei osu") {
      it("palautetaan tyhjä lista") {
        putOpiskeluOikeus(Map("päättymispäivä"-> "2016-01-09")) {
          authGet ("api/oppija?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01") {
            verifyResponseStatus(200)
            val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
            oppijat.length should equal(0)
          }
        }
      }
    }

    describe("Kun haetaan ei tuetulla parametrilla") {
      it("palautetaan HTTP 400") {
        authGet("api/oppija?eiTuettu=kyllä") {
          verifyResponseStatus(400, "Unsupported query parameter: eiTuettu")
        }
      }
    }

    describe("Kun haetaan ilman parametreja") {
      it("palautetaan kaikki oppijat") {
        putOpiskeluOikeus(Map("päättymispäivä"-> "2016-01-09")) {
          putOpiskeluOikeus(Map("päättymispäivä"-> "2013-01-09"), teija) {
            authGet ("api/oppija") {
              verifyResponseStatus(200)
              val oppijat: List[TorOppija] = Json.read[List[TorOppija]](response.body)
              oppijat.length should be >= 2
            }
          }
        }
      }
    }
  }

}
