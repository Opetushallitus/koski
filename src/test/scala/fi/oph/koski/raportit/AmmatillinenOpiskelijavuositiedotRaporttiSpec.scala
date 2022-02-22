package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Date
import java.time.LocalDate

class AmmatillinenOpiskelijavuositiedotRaporttiSpec
  extends AnyFreeSpec
    with RaportointikantaTestMethods
    with OpiskeluoikeusTestMethods
    with Matchers
    with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  private lazy val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Opiskelijavuositiedot" - {
    val oid = "1.2.246.562.15.123456"

    "raportti sisältää oikeat tiedot" in {
      val result = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))

      val aarnenOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammattilainen.oid).oid.get
      val aarnenRivi = result.find(_.opiskeluoikeusOid == aarnenOpiskeluoikeusOid)
      aarnenRivi shouldBe defined
      val rivi = aarnenRivi.get

      rivi.suorituksenTyyppi should equal("ammatillinentutkinto")
      rivi.koulutusmoduulit should equal("361902")
      rivi.koulutusmoduuliNimet should equal("Luonto- ja ympäristöalan perustutkinto")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.päätasonSuorituksenSuoritustapa should equal("Ammatillinen perustutkinto")
      rivi.opiskeluoikeudenAlkamispäivä should equal(Some(LocalDate.of(2012, 9, 1)))
      rivi.viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
      rivi.viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("valmistunut")
      rivi.opintojenRahoitukset should equal("4")
      rivi.opiskeluoikeusPäättynyt should equal(true)
      rivi.läsnäTaiValmistunutPäivät should equal(31 + 29 + 31 + 30 + 30 + 1) // Aarne graduated 31.5.2016, so count days from 1.1.2016 to 30.5.2016 + 31.5.2016
      rivi.arvioituPäättymispäivä should equal(Some(LocalDate.parse("2015-05-31")))
      rivi.ostettu should equal(false)
      rivi.yksiloity should equal(true)
    }

    "ostettu" in {
      val markkasenOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.markkanen.oid).oid.get
      val rivi = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.omnia, LocalDate.parse("2000-01-01"), LocalDate.parse("2000-01-02"))
        .find(_.opiskeluoikeusOid == markkasenOpiskeluoikeusOid)
        .get
      rivi.ostettu should equal(true)
    }

    "opiskelijavuoteen kuuluvat ja muut lomat lasketaan oikein" - {
      "lasna-tilaa ei lasketa lomaksi" in {
        AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-05-31"), "lasna", Date.valueOf("2016-01-15"))
        )) should equal((0, 0))
      }
      "lyhyt loma (alle 28 pv) lasketaan kokonaan opiskelijavuoteen" in {
        AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-05"), "loma", Date.valueOf("2016-02-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-06"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-02-06"))
        )) should equal((5, 0))
      }
      "pitkästä lomasta lasketaan 28 pv opiskelijavuoteen, loput muihin" in {
        AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-12-31"), "loma", Date.valueOf("2016-01-01"))
        )) should equal((28, 366 - 28))
      }
      "jos loma on alkanut ennen tätä aikajaksoa" - {
        "jos päiviä on tarpeeksi jäljellä, koko jakso lasketaan opiskelijavuoteen" in {
          AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2016-01-25"))
          )) should equal((14, 0))
        }
        "jos päiviä on jäljellä jonkin verran, osa jaksosta lasketaan opiskelijavuoteen" in {
          AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-03-31"), "loma", Date.valueOf("2016-01-31"))
          )) should equal((27, 33))
        }
        "jos päiviä ei ole jäljellä yhtään, koko jakso lasketaan muihin lomiin" in {
          AmmatillinenOpiskalijavuositiedotRaportti.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2015-12-01"))
          )) should equal((0, 14))
        }
      }
    }

    "opiskelijavuosikertymä lasketaan oikein" - {

      "läsnäolopäivät lasketaan mukaan" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"))
        )) should equal(31)
      }

      "osa-aikaisuus huomioidaan" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50)
        )) should equal(15.5)
      }

      "valmistumispäivä lasketaan mukaan" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(32)
      }

      "eroamispäivää ei lasketa mukaan" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "eronnut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(31)
      }

      "lomapäivät lasketaan mukaan" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-10"), "loma", Date.valueOf("2016-02-01"))
        )) should equal(41)
      }

      "valmistumispäivä lasketaan aina 100% läsnäolopäivänä, vaikka opinnot olisivat olleet osa-aikaisia" in {
        AmmatillinenOpiskalijavuositiedotRaportti.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true, osaAikaisuus = 50)
        )) should equal(16.5)
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/ammatillinenopiskelijavuositiedot", expectedRaporttiNimi = AmmatillinenOpiskelijavuositiedot.toString, expectedFileNamePrefix = "opiskelijavuositiedot")
    }

    "käyttöoikeudet" - {
      "raportin lataaminen vaatii käyttöoikeudet organisaatioon" in {
        authGet(s"api/raportit/ammatillinenopiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = omniaTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }

      "raportin lataaminen ei ole sallittu viranomais-käyttäjille (globaali-luku)" in {
        authGet(s"api/raportit/ammatillinenopiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = evira) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }
    }

    "raportin lataaminen asettaa koskiDownloadToken-cookien" in {
      authGet(s"api/raportit/ammatillinenopiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&lang=fi&password=dummy&downloadToken=test123") {
        verifyResponseStatusOk()
        val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiDownloadToken"))
        cookie shouldBe defined
        cookie.get should include("koskiDownloadToken=test123; Path=/")
      }
    }
  }
}
