package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class OpiskelijavuositiedotSpec extends FreeSpec with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  private val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Opiskelijavuositiedot" - {
    val oid = "1.2.246.562.15.123456"

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
      rivi.läsnäTaiValmistunutPäivät should equal(31 + 29 + 31 + 30 + 30 + 1) // Aarne graduated 31.5.2016, so count days from 1.1.2016 to 30.5.2016 + 31.5.2016
      rivi.arvioituPäättymispäivä should equal(Some(LocalDate.parse("2015-05-31")))
      rivi.ostettu should equal(false)
    }

    "ostettu" in {
      val markkasenOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.markkanen.oid).oid.get
      val rivi = Opiskelijavuositiedot.buildRaportti(raportointiDatabase, MockOrganisaatiot.omnia, LocalDate.parse("2000-01-01"), LocalDate.parse("2000-01-02"))
        .find(_.opiskeluoikeusOid == markkasenOpiskeluoikeusOid)
        .get
      rivi.ostettu should equal(true)
    }

    "opiskelijavuoteen kuuluvat ja muut lomat lasketaan oikein" - {
      "lasna-tilaa ei lasketa lomaksi" in {
        Opiskelijavuositiedot.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-05-31"), "lasna", Date.valueOf("2016-01-15"))
        )) should equal((0, 0))
      }
      "lyhyt loma (alle 28 pv) lasketaan kokonaan opiskelijavuoteen" in {
        Opiskelijavuositiedot.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-05"), "loma", Date.valueOf("2016-02-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-06"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-02-06"))
        )) should equal((5, 0))
      }
      "pitkästä lomasta lasketaan 28 pv opiskelijavuoteen, loput muihin" in {
        Opiskelijavuositiedot.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-12-31"), "loma", Date.valueOf("2016-01-01"))
        )) should equal((28, 366 - 28))
      }
      "jos loma on alkanut ennen tätä aikajaksoa" - {
        "jos päiviä on tarpeeksi jäljellä, koko jakso lasketaan opiskelijavuoteen" in {
          Opiskelijavuositiedot.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2016-01-25"))
          )) should equal((14, 0))
        }
        "jos päiviä on jäljellä jonkin verran, osa jaksosta lasketaan opiskelijavuoteen" in {
          Opiskelijavuositiedot.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-03-31"), "loma", Date.valueOf("2016-01-31"))
          )) should equal((27, 33))
        }
        "jos päiviä ei ole jäljellä yhtään, koko jakso lasketaan muihin lomiin" in {
          Opiskelijavuositiedot.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2015-12-01"))
          )) should equal((0, 14))
        }
      }
    }

    "opiskelijavuosikertymä lasketaan oikein" - {

      "läsnäolopäivät lasketaan mukaan" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"))
        )) should equal(31)
      }

      "osa-aikaisuus huomioidaan" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50)
        )) should equal(15.5)
      }

      "valmistumispäivä lasketaan mukaan" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(32)
      }

      "eroamispäivää ei lasketa mukaan" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "eronnut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(31)
      }

      "lomapäivät lasketaan mukaan" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-10"), "loma", Date.valueOf("2016-02-01"))
        )) should equal(41)
      }

      "valmistumispäivä lasketaan aina 100% läsnäolopäivänä, vaikka opinnot olisivat olleet osa-aikaisia" in {
        Opiskelijavuositiedot.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true, osaAikaisuus = 50)
        )) should equal(16.5)
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/opiskelijavuositiedot", expectedRaporttiNimi = "opiskelijavuositiedot", expectedFileNamePrefix = "opiskelijavuositiedot")
    }

    "käyttöoikeudet" - {
      "raportin lataaminen vaatii käyttöoikeudet organisaatioon" in {
        authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = omniaTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }

      "raportin lataaminen ei ole sallittu viranomais-käyttäjille (globaali-luku)" in {
        authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = evira) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }
    }

    "raportin lataaminen asettaa koskiDownloadToken-cookien" in {
      authGet(s"api/raportit/opiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy&downloadToken=test123") {
        verifyResponseStatusOk()
        val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiDownloadToken"))
        cookie shouldBe defined
        cookie.get should include("koskiDownloadToken=test123;Path=/")
      }
    }
  }

  override def beforeAll(): Unit = loadRaportointikantaFixtures
}
