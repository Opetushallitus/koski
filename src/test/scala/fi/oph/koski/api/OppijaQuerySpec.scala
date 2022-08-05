package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.{ExampleData, PerusopetusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaQuerySpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen with QueryTestMethods with Matchers {
  import fi.oph.koski.util.DateOrdering._
  val teija = KoskiSpecificMockOppijat.teija
  val eero = KoskiSpecificMockOppijat.eero

  "Kyselyrajapinta" - {
    "kun haku osuu" - {
      "päättymispäivämäärä" in {
        resetFixtures
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016, 1, 9)), eero)
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2013, 1, 9)), teija)

        val queryString: String = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31"
        val oppijat = queryOppijat("?" + queryString)
        val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap { oppija =>
          oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].hetu.get, _))
        }
        päättymispäivät should contain(("010101-123N", LocalDate.parse("2016-01-09")))
        päättymispäivät.map(_._2).foreach { pvm => pvm should (be >= LocalDate.parse("2016-01-01") and be <= LocalDate.parse("2016-12-31")) }
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("hakuEhto" -> queryString)))
      }
      "alkamispäivämäärä" in {
        resetFixtures
        insert(makeOpiskeluoikeus(date(2100, 1, 2)), eero)
        insert(makeOpiskeluoikeus(date(2110, 1, 1)), teija)
        val alkamispäivät = queryOppijat("?opiskeluoikeusAlkanutAikaisintaan=2100-01-02&opiskeluoikeusAlkanutViimeistään=2100-01-02")
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
      "mitätöityjä ei palauteta" in {
        resetFixtures
        val ooCount = queryOppijat().flatMap(_.opiskeluoikeudet).length
        val opiskeluoikeus = makeOpiskeluoikeus()
        insert(opiskeluoikeus.copy(tila =
          opiskeluoikeus.tila.copy(opiskeluoikeusjaksot =
            opiskeluoikeus.tila.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(LocalDate.now, ExampleData.opiskeluoikeusMitätöity)
          )
        ), KoskiSpecificMockOppijat.koululainen)
        queryOppijat().flatMap(_.opiskeluoikeudet).length should equal(ooCount)
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

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        resetFixtures
        vankilaopetuksessa(haeOpiskeluoikeudetOppijanNimellä("Eero", "Esimerkki", stadinAmmattiopistoKatselija)) should equal(Some(List(Aikajakso(date(2019, 5, 30), None))))
      }

      "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        vankilaopetuksessa(haeOpiskeluoikeudetOppijanNimellä("Eero", "Esimerkki", user = stadinVastuukäyttäjä)) should equal(None)
      }
    }

    "Lasketut kentät" - {
      "Palautetaan käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli " in {
        val ensimmäisenOsasuorituksetArviointi: Arviointi = haeOpiskeluoikeudetOppijanNimellä("Anneli", "Amikseenvalmistautuja", user = stadinAmmattiopistoKatselija)
          .head.suoritukset.head.osasuoritusLista.head.viimeisinArviointi.get
        ensimmäisenOsasuorituksetArviointi.hyväksytty should be(true)
      }

      "Palautetaan käyttäjälle jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli " in {
        val ensimmäisenOsasuorituksetArviointi: Arviointi = haeOpiskeluoikeudetOppijanNimellä("Anneli", "Amikseenvalmistautuja", user = stadinVastuukäyttäjä)
          .head.suoritukset.head.osasuoritusLista.head.viimeisinArviointi.get
        ensimmäisenOsasuorituksetArviointi.hyväksytty should be(true)
      }
    }

    "luokkahaku" - {
      "luokan osittaisella tai koko nimellä" in {
        resetFixtures
        insert(PerusopetusExampleData.opiskeluoikeus(
          alkamispäivä = date(2000, 1, 2),
          päättymispäivä = None,
          suoritukset = List(PerusopetusExampleData.kahdeksannenLuokanSuoritus.copy(luokka = "8C"))
        ), eero)
        insert(PerusopetusExampleData.opiskeluoikeus(
          alkamispäivä = date(2000, 1, 2),
          päättymispäivä = None,
          suoritukset = List(PerusopetusExampleData.kahdeksannenLuokanSuoritus.copy(luokka = "8D"))
        ), teija)
        queryOppijat("?opiskeluoikeusAlkanutViimeistään=2000-01-02&luokkahaku=8").length should equal(2)
        queryOppijat("?opiskeluoikeusAlkanutViimeistään=2000-01-02&luokkahaku=8c").length should equal(1)
      }
    }

    "Kun haku ei osu" - {
      "palautetaan tyhjä lista" in {
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), eero)
        val oppijat = queryOppijat("?opiskeluoikeusPäättynytViimeistään=2014-04-30&opiskeluoikeusPäättynytAikaisintaan=2014-01-01")
        oppijat.length should equal(0)
        resetFixtures
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
        verifyResponseStatusOk()
      }
    }
  }

  private def haeOpiskeluoikeudetOppijanNimellä(etunimet: String, sukunimi: String, user: UserWithPassword) = {
    queryOppijat(s"?opiskeluoikeudenTyyppi=ammatillinenkoulutus", user).collectFirst { case Oppija(h: TäydellisetHenkilötiedot, oos) if h.etunimet == etunimet && h.sukunimi == sukunimi =>
     oos.collect { case oo: AmmatillinenOpiskeluoikeus => oo }
    }.toList.flatten
  }

  def vankilaopetuksessa(oos: List[AmmatillinenOpiskeluoikeus]): Option[List[Aikajakso]] = oos.head.lisätiedot.flatMap(_.vankilaopetuksessa)
}

