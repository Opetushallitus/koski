package fi.oph.koski.api

import java.time.LocalDate
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.{FunSpec, Matchers}
import java.time.LocalDate.{of => date}

class OppijaQuerySpec extends FunSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers {
  val teija = UusiHenkilö("150995-914X", "Teija", "Teija", "Tekijä")

  AuditLogTester.setup

  describe("Kyselyrajapinta") {
    describe("kun haku osuu") {
      it("palautetaan hakutulokset") {
        resetFixtures
        putOpiskeluOikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9))) {
          verifyResponseStatus(200)
          putOpiskeluOikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), teija) {
            verifyResponseStatus(200)
            val queryString: String = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31"
            authGet ("api/oppija?" + queryString) {
              verifyResponseStatus(200)
              val oppijat: List[Oppija] = Json.read[List[Oppija]](response.body)
              val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap{oppija =>
                oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].hetu, _))
              }
              päättymispäivät should contain(("010101-123N", LocalDate.parse("2016-01-09")))
              päättymispäivät.map(_._2).foreach { pvm => pvm should (be >= LocalDate.parse("2016-01-01") and be <= LocalDate.parse("2016-12-31"))}
              AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_HAKU", "hakuEhto" -> queryString))
            }
          }
        }
      }
    }

    describe("Kun haku ei osu") {
      it("palautetaan tyhjä lista") {
        putOpiskeluOikeus(defaultOpiskeluoikeus.copy(päättymispäivä = Some(date(2016,1,9)))) {
          authGet ("api/oppija?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01") {
            verifyResponseStatus(200)
            val oppijat: List[Oppija] = Json.read[List[Oppija]](response.body)
            oppijat.length should equal(0)
          }
        }
      }
    }

    describe("Kun haetaan ei tuetulla parametrilla") {
      it("palautetaan HTTP 400") {
        authGet("api/oppija?eiTuettu=kyllä") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: eiTuettu"))
        }
      }
    }

    describe("Kun haetaan ilman parametreja") {
      it("palautetaan kaikki oppijat") {
        putOpiskeluOikeus(defaultOpiskeluoikeus.copy(päättymispäivä = Some(date(2016,1,9)))) {
          putOpiskeluOikeus(defaultOpiskeluoikeus.copy(päättymispäivä = Some(date(2013,1,9))), teija) {
            authGet ("api/oppija") {
              verifyResponseStatus(200)
              val oppijat: List[Oppija] = Json.read[List[Oppija]](response.body)
              oppijat.length should be >= 2
            }
          }
        }
      }
    }
  }

  implicit val localDateOrdering: Ordering[LocalDate] = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }
}
