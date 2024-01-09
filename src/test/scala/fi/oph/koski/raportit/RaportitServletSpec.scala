package fi.oph.koski.raportit


import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.json4s.JArray
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RaportitServletSpec extends AnyFreeSpec with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "Mahdolliset raportit -API" - {

    "Oppilaitoksen mahdolliset raportit" - {
      "sallii opiskelijavuositiedot ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(AmmatillinenOpiskelijavuositiedot.toString)
        }
      }
      "sallii suoritustietojen tarkistuksen ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(AmmatillinenTutkintoSuoritustietojenTarkistus.toString)
        }
      }
      "sallii muu ammatillisen koulutuksen raportin ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(MuuAmmatillinenKoulutus.toString)
        }
      }
      "sallii topks ammatillisen koulutuksen raportin ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(TOPKSAmmatillinen.toString)
        }
      }
      "sallii tuva perusopetuksen raportin ammatilliselle oppilaitokselle, joka järjestää tuva-koulutusta perusopetuksen järjestämisluvalla" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(TuvaPerusopetuksenOppijaMääräRaportti.toString)
        }
      }
      "sallii tuva perusopetuksen suoritustiedot-raportin ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(TuvaSuoritustietojenTarkistus.toString)
        }
      }
      "sallii suoritustietojen tarkistuksen osittaisista ammatillisista tutkinnoista ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain(AmmatillinenOsittainenSuoritustietojenTarkistus.toString)
        }
      }
      "sallii esiopetuksen raportin esiopetusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(jyväskylänNormaalikoulu) { raportit =>
          raportit should contain(EsiopetuksenRaportti.toString)
        }
      }
      "sallii perusopetuksenvuosiluokka raportin perusopetusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(jyväskylänNormaalikoulu) { raportit =>
          raportit should contain(PerusopetuksenVuosiluokka.toString)
        }
      }
      "sallii perusopetuksen lisäopetuksen oppijamäärä raportin perusopetuksen lisäopetusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(jyväskylänNormaalikoulu) { raportit =>
          raportit should contain(PerusopetuksenLisäopetuksenOppijaMääräRaportti.toString)
        }
      }
      "sallii lukion raportin luki-opetusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(jyväskylänNormaalikoulu) { raportit =>
          raportit should contain(LukionSuoritustietojenTarkistus.toString)
        }
      }
      "sallii IB-tutkinnon suoritustietojen raportin IB-tutkinnon suoritusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(ressunLukio) { raportit =>
          raportit should contain(IBSuoritustietojenTarkistus.toString)
        }
      }
    }

      "Käyttäjän mahdolliset raportit" - {
      "Esiopetus-oikeuksilla voi valita vain esiopetuksen raporteista" in {
        verifyMahdollisetRaportit(helsinginKaupunki, helsinginKaupunkiPalvelukäyttäjä) { raportit =>
          raportit.length should be > 1
        }
        verifyMahdollisetRaportit(helsinginKaupunki, user = esiopetusTallentaja) { raportit =>
          raportit should contain theSameElementsAs (List(EsiopetuksenRaportti.toString, EsiopetuksenOppijaMäärienRaportti.toString()))
        }
      }
    }

    "Käyttäjä oikeuksien tarkistus" - {
      "sallii koulutustoimijan oikeuksilla hakiessa koulutustoimijan alla olevien oppilaitosten raportit" in {
        verifyMahdollisetRaportit(helsinginKaupunki, user = helsinginKaupunkiPalvelukäyttäjä) { raportit => {
           raportit should contain theSameElementsAs(List(
             EsiopetuksenRaportti.toString,
             EsiopetuksenOppijaMäärienRaportti.toString,
             PerusopetuksenVuosiluokka.toString,
             PerusopetuksenOppijaMääräRaportti.toString,
             TuvaPerusopetuksenOppijaMääräRaportti.toString,
             PerusopetuksenLisäopetuksenOppijaMääräRaportti.toString,
             LukioDiaIbInternationalESHOpiskelijamaarat.toString,
             LukioKurssikertyma.toString,
             LuvaOpiskelijamaarat.toString,
             LukioOpintopistekertyma.toString,
           ))
          }
        }
      }
      "koulutustoimijan oidilla haettessa vaaditaan koulutustoimijan oikeudet" in {
        authGet(s"${mahdollisetRaportitUrl}${helsinginKaupunki}") {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio())
        }
      }
      "koulutustoimijan oikeuksilla voi hakea vain oman koulutustoiminta-alueen raportteja" in {
        authGet(s"${mahdollisetRaportitUrl}${helsinginKaupunki}", user = omniaPääkäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio())
        }
      }
      "koulutustoimijan oikeuksilla ei voi hakea toisen oppilaitoksen raportteja" in {
        authGet(s"${mahdollisetRaportitUrl}${jyväskylänNormaalikoulu}", user = helsinginKaupunkiPalvelukäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio())
        }
      }
      "ei voi ladata raporttia jos raportin opiskeluoikeuden tyyppiin ei ole oikeuksia" in {
        authGet(s"api/raportit/lukionsuoritustietojentarkistus?oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = perusopetusTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi())
        }
      }
      "koulutustoimijan oikeuksilla voi ladata esiopetuksen ostopalveluiden raportin" in {
        authGet(s"api/raportit/esiopetus?oppilaitosOid=${KoskiApplicationForTests.organisaatioService.ostopalveluRootOid}&paiva=2022-06-07&lang=fi&password=dummy", user = helsinkiTallentaja) {
          verifyResponseStatusOk()
        }
      }
      "koulutustoimijan oikeuksilla ei voi ladata perusopetuksen raporttia peruskoululle, joka on esiopetuksen ostopalvelun oppilaitos (eikä koulutustoimijan oma organisaatio)" in {
        authGet(s"api/raportit/perusopetuksenvuosiluokka?oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&paiva=2022-06-07&vuosiluokka=9&lang=fi&password=dummy", user = helsinkiTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio())
        }
      }
    }
  }

  "Organisaatiohierarkia raporttityypeillä -API" - {
    "Juuriorganisaatiot pääkäyttäjälle" in {
      verifyOrganisaatiohierarkia(paakayttaja) { hierarkia => {
        hierarkia.length shouldEqual 24
      }}
    }

    "Juuriorganisaatiot testikäyttäjälle kalle" in {
      verifyOrganisaatiohierarkia(kalle) { hierarkia => {
        hierarkia.length shouldEqual 24
      }}
    }

    "Juuriorganisaatiot Helsingin palvelukäyttäjälle" in {
      verifyOrganisaatiohierarkia(helsinginKaupunkiPalvelukäyttäjä) { hierarkia => {
        hierarkia.length shouldEqual 2
      }}
    }
  }

  private val mahdollisetRaportitUrl = "api/raportit/mahdolliset-raportit/"
  private val organisaatiotJaRaporttityypitUrl = "api/raportit/organisaatiot-ja-raporttityypit"

  private def verifyMahdollisetRaportit(organisaatio: String, user: UserWithPassword = defaultUser)(f: Seq[Any] => Unit) =
    verifyArrayResponse(s"${mahdollisetRaportitUrl}${organisaatio}", user, f)

  private def verifyOrganisaatiohierarkia(user: UserWithPassword = defaultUser)(f: Seq[Any] => Unit) =
    verifyArrayResponse(organisaatiotJaRaporttityypitUrl, user, f)

  private def verifyArrayResponse(uri: String, user: UserWithPassword = defaultUser, f: Seq[Any] => Unit) = {
    authGet(uri, user) {
      verifyResponseStatusOk()
      val parsedJson = JsonMethods.parse(body)
      parsedJson shouldBe a[JArray]
      val response = parsedJson.asInstanceOf[JArray].values

      f(response)
    }
  }
}
