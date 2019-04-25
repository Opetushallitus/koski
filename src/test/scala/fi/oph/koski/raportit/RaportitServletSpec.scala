package fi.oph.koski.raportit


import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.json4s.JArray
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class RaportitServletSpec extends FreeSpec with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  private val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Mahdolliset raportit -API" - {
    "sallii opiskelijavuositiedot ammatilliselle oppilaitokselle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.stadinAmmattiopisto}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson.asInstanceOf[JArray].values should contain("opiskelijavuositiedot")
      }
    }
    "sallii suoritustietojen tarkistuksen ammatilliselle oppilaitokselle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.stadinAmmattiopisto}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson.asInstanceOf[JArray].values should contain("suoritustietojentarkistus")
      }
    }
    "sallii suoritustietojen tarkistuksen osittaisista ammatillisista tutkinnoista ammatilliselle oppilaitokselle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.stadinAmmattiopisto}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson.asInstanceOf[JArray].values should contain("ammatillinenosittainensuoritustietojentarkistus")
      }
    }
    "sallii perusopetuksenvuosiluokka raportin perusopetusta järjestävälle oppilaitokselle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.jyväskylänNormaalikoulu}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson.asInstanceOf[JArray].values should contain("perusopetuksenvuosiluokka")
      }
    }
    "ei salli mitään nykyisistä raporteista lukiolle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.ressunLukio}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson should equal(JArray(List.empty))
      }
    }
  }


  override def beforeAll(): Unit = loadRaportointikantaFixtures
}
