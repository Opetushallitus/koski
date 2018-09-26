package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, omniaTallentaja, evira}
import fi.oph.koski.log.AuditLogTester
import org.json4s.JArray
import org.json4s.jackson.JsonMethods

class RaportitSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

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
    "ei salli mitään nykyisistä raporteista lukiolle" in {
      authGet(s"api/raportit/mahdolliset-raportit/${MockOrganisaatiot.ressunLukio}") {
        verifyResponseStatusOk()
        val parsedJson = JsonMethods.parse(body)
        parsedJson shouldBe a[JArray]
        parsedJson should equal(JArray(List.empty))
      }
    }
  }

  "Opiskelijavuositiedot" - {
    "raportti sisältää oikeat tiedot" in {
      val result = Opiskelijavuositiedot.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))

      val aarnenOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ammattilainen.oid).oid.get
      val aarnenRivi = result.find(_.opiskeluoikeusOid == aarnenOpiskeluoikeusOid)
      aarnenRivi shouldBe defined
      val rivi = aarnenRivi.get

      rivi.koulutusmoduulit should equal("361902")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.viimeisinOpiskeluoikeudenTila should equal("valmistunut")
      rivi.opintojenRahoitukset should equal("4")
      rivi.opiskeluoikeusPäättynyt should equal(true)
      rivi.läsnäPäivät should equal(31 + 29 + 31 + 30 + 30) // Aarne graduated 31.5.2016, so count days from 1.1.2016 to 30.5.2016
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      val queryString1 = s"oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31"
      val queryString2 = "password=dummy&downloadToken=test123"
      authGet(s"api/raportit/opiskelijavuositiedot?$queryString1&$queryString2") {
        verifyResponseStatusOk()
        val ENCRYPTED_XLSX_PREFIX = Array(0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1).map(_.toByte)
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=opiskelijavuositiedot&$queryString1")))
      }
    }

    "käyttöoikeudet" - {
      "raportin lataaminen vaatii käyttöoikeudet organisaatioon" in {
        authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31", user = omniaTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }

      // temporary restriction
      "raportin lataaminen on sallittu vain pilottikäyttäjille" in {
        authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31", user = stadinAmmattiopistoKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden("Ei sallittu tälle käyttäjälle"))
        }
      }

      "raportin lataaminen ei ole sallittu viranomais-käyttäjille (globaali-luku)" in {
        authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31", user = evira) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }
    }

    "raportin lataaminen asettaa koskiDownloadToken-cookien" in {
      authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&downloadToken=test123") {
        verifyResponseStatusOk()
        val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiDownloadToken"))
        cookie shouldBe defined
        cookie.get should include("koskiDownloadToken=test123;Path=/")
      }
    }
  }

  override def beforeAll(): Unit = {
    authGet("api/raportointikanta/clear") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/opiskeluoikeudet") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/henkilot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/organisaatiot") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/koodistot") { verifyResponseStatusOk() }
  }
}
