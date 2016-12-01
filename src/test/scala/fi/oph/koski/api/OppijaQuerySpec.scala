package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.date.DateOrdering
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.{FunSpec, Matchers}

class OppijaQuerySpec extends FunSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with QueryTestMethods with Matchers {
  import DateOrdering._
  val teija = UusiHenkilö("251019-039B", "Teija", "Teija", "Tekijä")

  describe("Kyselyrajapinta") {
    describe("kun haku osuu") {
      it("palautetaan hakutulokset") {
        resetFixtures
        putOpiskeluOikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9))) {
          verifyResponseStatus(200)
          putOpiskeluOikeus(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), teija) {
            verifyResponseStatus(200)
            val queryString: String = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31"
            val oppijat = queryOppijat("?" + queryString)
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

    describe("Kun haku ei osu") {
      it("palautetaan tyhjä lista") {
        putOpiskeluOikeus(defaultOpiskeluoikeus.copy(päättymispäivä = Some(date(2016,1,9)))) {
          val oppijat = queryOppijat("?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01")
          oppijat.length should equal(0)
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
            val oppijat = queryOppijat()
            oppijat.length should be >= 2
          }
        }
      }
    }
  }
}
