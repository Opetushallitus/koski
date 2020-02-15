package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.{ExampleData, PerusopetusExampleData}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.json4s.JsonAST.{JArray, JBool}
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class OppijaQuerySpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with QueryTestMethods with Matchers {
  import fi.oph.koski.util.DateOrdering._
  val teija = MockOppijat.teija
  val eero = MockOppijat.eero

  "Kyselyrajapinta" - {
    "kun haku osuu" - {
      "päättymispäivämäärä" in {
        resetFixtures
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2016,1,9)), eero)
        insert(päättymispäivällä(defaultOpiskeluoikeus, date(2013,1,9)), teija)

        val queryString: String = "opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31"
        val oppijat = queryOppijat("?" + queryString)
        val päättymispäivät: List[(String, LocalDate)] = oppijat.flatMap {oppija =>
          oppija.opiskeluoikeudet.flatMap(_.päättymispäivä).map((oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot].hetu.get, _))
        }
        päättymispäivät should contain(("010101-123N", LocalDate.parse("2016-01-09")))
        päättymispäivät.map(_._2).foreach { pvm => pvm should (be >= LocalDate.parse("2016-01-01") and be <= LocalDate.parse("2016-12-31"))}
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
        ), MockOppijat.koululainen)
        queryOppijat().flatMap(_.opiskeluoikeudet).length should equal(ooCount)
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

    "Luottamuksellinen data" - {
      "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        vankilaopetuksessa(haeNimellä("Esimerkki", stadinAmmattiopistoKatselija)) should equal(Some(List(Aikajakso(date(2001,1,1), None))))
      }

      "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
        vankilaopetuksessa(haeNimellä("Esimerkki", stadinVastuukäyttäjä)) should equal(None)
      }
    }

    "Lasketut kentät" - {
      "Palautetaan käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli " in {
        haeNimellä(MockOppijat.valma.sukunimi, stadinAmmattiopistoKatselija).head.opiskeluoikeudet.head.suoritukset.head.osasuoritusLista.head.arviointi.head.head.hyväksytty should equal(true)
      }
      "Palautetaan käyttäjälle jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli " in {
        haeNimellä(MockOppijat.valma.sukunimi, stadinVastuukäyttäjä).head.opiskeluoikeudet.head.suoritukset.head.osasuoritusLista.head.arviointi.head.head.hyväksytty should equal(true)
      }
    }

  }

  private def insert(opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö) = {
    putOpiskeluoikeus(opiskeluoikeus, henkilö) {
      verifyResponseStatusOk()
    }
  }

  private def haeNimellä(sukunimi: String, user: UserWithPassword) =
    queryOppijat( user = user).filter {
      case Oppija(h: NimellinenHenkilö, _) => h.sukunimi == sukunimi
      case _ => false
    }

  private def vankilaopetuksessa(queryResult: Seq[Oppija]): Option[List[Aikajakso]] = queryResult.toList match {
    case List(oppija) =>
      oppija.opiskeluoikeudet(0).asInstanceOf[AmmatillinenOpiskeluoikeus].lisätiedot.flatMap(_.vankilaopetuksessa)
    case oppijat =>
      fail("Unexpected number of results: " + oppijat.length)
  }
}

