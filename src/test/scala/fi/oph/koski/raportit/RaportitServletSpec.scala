package fi.oph.koski.raportit


import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.json4s.{JArray}
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class RaportitServletSpec extends FreeSpec with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  "Mahdolliset raportit -API" - {

    "Oppilaitoksen mahdolliset raportit" - {
      "sallii opiskelijavuositiedot ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain("opiskelijavuositiedot")
        }
      }
      "sallii suoritustietojen tarkistuksen ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain("suoritustietojentarkistus")
        }
      }
      "sallii suoritustietojen tarkistuksen osittaisista ammatillisista tutkinnoista ammatilliselle oppilaitokselle" in {
        verifyMahdollisetRaportit(stadinAmmattiopisto) { raportit =>
          raportit should contain("ammatillinenosittainensuoritustietojentarkistus")
        }
      }
      "sallii perusopetuksenvuosiluokka raportin perusopetusta järjestävälle oppilaitokselle" in {
        verifyMahdollisetRaportit(jyväskylänNormaalikoulu) { raportit =>
          raportit should contain("perusopetuksenvuosiluokka")
        }
      }
      "ei salli mitään nykyisistä raporteista lukiolle" in {
        verifyMahdollisetRaportit(ressunLukio) { raportit =>
          raportit should equal(List.empty)
        }
      }
    }

    "Käyttäjä oikeuksien tarkistus" - {
      "sallii koulutustoimijan oikeuksilla hakiessa koulutustoimijan alla olevien oppilaitosten raportit perusopetukselle" in {
        verifyMahdollisetRaportit(helsinginKaupunki, user = helsinginKaupunkiPalvelukäyttäjä) { raportit => {
           raportit should equal(Seq("perusopetuksenvuosiluokka"))
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
