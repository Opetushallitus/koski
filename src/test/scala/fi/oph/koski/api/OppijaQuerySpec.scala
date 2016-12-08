package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.date.DateOrdering
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.{FunSpec, Matchers}

class OppijaQuerySpec extends FunSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with QueryTestMethods with Matchers {
  import DateOrdering._
  val teija = MockOppijat.teija.vainHenkilötiedot
  val eero = MockOppijat.eero.vainHenkilötiedot

  describe("Kyselyrajapinta") {
    describe("kun haku osuu") {
      it("päättymispäivämäärä") {
        resetFixtures
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), eero)
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), teija)

        val queryString: String = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31"
        val oppijat = queryOppijat("?" + queryString)
        val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap {oppija =>
          oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].hetu, _))
        }
        päättymispäivät should contain(("010101-123N", LocalDate.parse("2016-01-09")))
        päättymispäivät.map(_._2).foreach { pvm => pvm should (be >= LocalDate.parse("2016-01-01") and be <= LocalDate.parse("2016-12-31"))}
        AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "OPISKELUOIKEUS_HAKU", "hakuEhto" -> queryString))

      }
      it("alkamispäivämäärä") {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), teija)
        val alkamispäivät = queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeusAlkanutViimeistään=2100-01-02")
            .flatMap(_.opiskeluoikeudet.flatMap(_.alkamispäivä))
        alkamispäivät should equal(List(date(2100, 1, 2)))
      }
      it("tutkinnon tila") {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)).copy(suoritukset = List(AmmatillinenExampleData.ympäristöalanPerustutkintoValmis())), eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), teija)
        val alkamispäivät = queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTila=VALMIS")
          .flatMap(_.opiskeluoikeudet.flatMap(_.alkamispäivä))
        alkamispäivät should equal(List(date(2100, 1, 2)))
      }
      it("opiskeluoikeuden tyyppi") {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=ammatillinenkoulutus").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=perusopetus").length should equal(0)
      }
      it("suorituksen tyyppi") {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTyyppi=ammatillinentutkinto").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTyyppi=lukionoppimaara").length should equal(0)
      }
      it("opiskeluoikeuden tila") {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTila=lasna").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTila=eronnut").length should equal(0)
      }
      describe("tutkintohaku") {
        it("tutkinnon nimi") {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&tutkintohaku=autoalan").length should equal(1)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&tutkintohaku=blah").length should equal(0)
        }
        it("osaamisala ja tutkintonimike") {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)).copy(suoritukset = List(AmmatillinenExampleData.ympäristöalanPerustutkintoValmis())), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&tutkintohaku=ympäristöalan%20osaamis").length should equal(1)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&tutkintohaku=ympäristönhoitaj").length should equal(1)
        }
        it("liian lyhyt hakusana") {
          authGet("api/oppija?tutkintohaku=au") {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
          }
        }
      }
      describe("toimipistehaku") {
        it("toimipisteen OID:lla") {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&toimipiste=1.2.246.562.10.42456023292").length should equal(1)
        }

        it("oppilaitoksen OID:lla") {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&toimipiste=1.2.246.562.10.52251087186").length should equal(1)
        }

        it("jos organisatiota ei löydy") {
          authGet("api/oppija?toimipiste=1.2.246.562.10.42456023000") {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.oppilaitostaEiLöydy("Oppilaitosta/koulutustoimijaa/toimipistettä ei löydy: 1.2.246.562.10.42456023000"))
          }
        }
      }
    }

    describe("Kun haku ei osu") {
      it("palautetaan tyhjä lista") {
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), eero)
        val oppijat = queryOppijat("?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01")
        oppijat.length should equal(0)
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
        val oppijat = queryOppijat()
        oppijat.length should be >= 2
      }
    }

    def insert(opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö) = {
      putOpiskeluOikeus(opiskeluoikeus, henkilö) {
        verifyResponseStatus(200)
      }
    }
  }
}
