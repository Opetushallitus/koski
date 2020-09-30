package fi.oph.koski.raportit


import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.json4s.JArray
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class RaportitServletSpec extends FreeSpec with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

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
             LukioDiaIbInternationalOpiskelijamaarat.toString,
             LukioKurssikertyma.toString,
             LuvaOpiskelijamaarat.toString
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
    }
  }

  private val mahdollisetRaportitUrl = "api/raportit/mahdolliset-raportit/"

  private def verifyMahdollisetRaportit(organisaatio: String, user: UserWithPassword = defaultUser)(f: Seq[Any] => Unit) = {
    authGet(s"${mahdollisetRaportitUrl}${organisaatio}", user) {
      verifyResponseStatusOk()
      val parsedJson = JsonMethods.parse(body)
      parsedJson shouldBe a[JArray]
      val raportit = parsedJson.asInstanceOf[JArray].values

      f(raportit)
    }
  }

  override def beforeAll(): Unit = loadRaportointikantaFixtures
}
