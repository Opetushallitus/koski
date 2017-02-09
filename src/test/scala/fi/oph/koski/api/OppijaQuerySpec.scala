package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.date.DateOrdering
import fi.oph.koski.documentation.{AmmatillinenExampleData, PerusopetusExampleData}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class OppijaQuerySpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with QueryTestMethods with Matchers {
  import DateOrdering._
  val teija = MockOppijat.teija.vainHenkilötiedot
  val eero = MockOppijat.eero.vainHenkilötiedot

  "Kyselyrajapinta" - {
    "kun haku osuu" - {
      "nimihaku" - {
        "sukunimellä tai etunimellä" in {
          queryOppijat("?nimihaku=eerola").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi) should equal(List("Jouni Eerola"))
          queryOppijat("?nimihaku=eero").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi).sorted should equal(List("Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström", "Jouni Eerola"))
        }
        "sukunimen tai etunimen osalla" in {
          queryOppijat("?nimihaku=eerol").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi) should equal(List("Jouni Eerola"))
          queryOppijat("?nimihaku=eer").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi).sorted should equal(List("Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström", "Jouni Eerola"))
        }
        "etunimi-sukunimiyhdistelmällä" in {
          queryOppijat("?nimihaku=jouni%20eerola").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi) should equal(List("Jouni Eerola"))
        }
        "osittaisten nimien yhdistelmällä" in {
          queryOppijat("?nimihaku=jou%20eer").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi) should equal(List("Jouni Eerola"))
        }
        "aksentit & moniosaiset nimet" in {
          queryOppijat("?nimihaku=eero%20fager").map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].kokonimi) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
        }
      }
      "päättymispäivämäärä" in {
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
      "alkamispäivämäärä" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), teija)
        val alkamispäivät = queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeusAlkanutViimeistään=2100-01-02")
            .flatMap(_.opiskeluoikeudet.flatMap(_.alkamispäivä))
        alkamispäivät should equal(List(date(2100, 1, 2)))
      }
      "tutkinnon tila" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)).copy(suoritukset = List(AmmatillinenExampleData.ympäristöalanPerustutkintoValmis())), eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), teija)
        val alkamispäivät = queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTila=VALMIS")
          .flatMap(_.opiskeluoikeudet.flatMap(_.alkamispäivä))
        alkamispäivät should equal(List(date(2100, 1, 2)))
      }
      "opiskeluoikeuden tyyppi" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=ammatillinenkoulutus").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTyyppi=perusopetus").length should equal(0)
      }
      "suorituksen tyyppi" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTyyppi=ammatillinentutkinto").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&suorituksenTyyppi=lukionoppimaara").length should equal(0)
      }
      "opiskeluoikeuden tila" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTila=lasna").length should equal(1)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeudenTila=eronnut").length should equal(0)
      }
      "toimipistehaku" - {
        "toimipisteen OID:lla" in {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&toimipiste=1.2.246.562.10.42456023292").length should equal(1)
        }

        "oppilaitoksen OID:lla" in {
          resetFixtures
          insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
          queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&toimipiste=1.2.246.562.10.52251087186").length should equal(1)
        }

        "jos organisatiota ei löydy" in {
          authGet("api/oppija?toimipiste=1.2.246.562.10.42456023000") {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.oppilaitostaEiLöydy("Oppilaitosta/koulutustoimijaa/toimipistettä ei löydy: 1.2.246.562.10.42456023000"))
          }
        }
      }
    }

    "luokkahaku" - {
      "luokan osittaisella tai koko nimellä" in {
        resetFixtures
        insert(PerusopetusExampleData.opiskeluoikeus(alkamispäivä = date(2100, 1, 2), päättymispäivä = None, suoritukset = List(PerusopetusExampleData.kahdeksannenLuokanSuoritus.copy(luokka = "8C"))), eero)
        insert(PerusopetusExampleData.opiskeluoikeus(alkamispäivä = date(2100, 1, 2), päättymispäivä = None, suoritukset = List(PerusopetusExampleData.kahdeksannenLuokanSuoritus.copy(luokka = "8D"))), teija)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&luokkahaku=8").length should equal(2)
        queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&luokkahaku=8c").length should equal(1)
      }
    }

    "Kun haku ei osu" - {
      "palautetaan tyhjä lista" in {
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), eero)
        val oppijat = queryOppijat("?opiskeluoikeusPäättynytViimeistään=2014-12-31&opiskeluoikeusPäättynytAikaisintaan=2014-01-01")
        oppijat.length should equal(0)
      }
    }

    "Kun haetaan ei tuetulla parametrilla" - {
      "palautetaan HTTP 400" in {
        authGet("api/oppija?eiTuettu=kyllä") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: eiTuettu"))
        }
      }
    }

    "Kun haetaan ilman parametreja" - {
      "palautetaan kaikki oppijat" in {
        val oppijat = queryOppijat()
        oppijat.length should be >= 2
      }
    }

    def insert(opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö) = {
      putOpiskeluoikeus(opiskeluoikeus, henkilö) {
        verifyResponseStatus(200)
      }
    }
  }
}

