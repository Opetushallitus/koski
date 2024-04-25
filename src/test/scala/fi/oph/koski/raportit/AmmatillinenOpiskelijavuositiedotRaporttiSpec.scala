package fi.oph.koski.raportit

import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.{ROpiskeluoikeusAikajaksoRow, RaportointikantaTestMethods}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeusjakso, AmmatillisenOpiskeluoikeudenLisätiedot, Maksuttomuus, OikeuttaMaksuttomuuteenPidennetty, Oppija}
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
    with BeforeAndAfterAll
    with DirtiesFixtures
    with OpiskeluoikeusTestMethodsAmmatillinen {

  override protected def alterFixture(): Unit = {
    putOppija(Oppija(vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa, List(
      AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(valmistumispäivä = LocalDate.of(2028, 3, 1)).copy(
          lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = None,
          maksuttomuus = Some(List(
            Maksuttomuus(LocalDate.of(2026, 2, 1), Some(LocalDate.of(2027, 1, 31)), true),
            Maksuttomuus(LocalDate.of(2027, 2, 1), Some(LocalDate.of(2028, 1, 31)), true),
            Maksuttomuus(LocalDate.of(2028, 2, 1), None, true),
          )),
          oikeuttaMaksuttomuuteenPidennetty = Some(List(
            OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2026, 2, 1), LocalDate.of(2027, 1, 31)),
            OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2027, 2, 1), LocalDate.of(2028, 1, 31)),
            OikeuttaMaksuttomuuteenPidennetty(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 3, 1)),
          ))
        ))
      )
    ))) {
      verifyResponseStatusOk()
    }
    reloadRaportointikanta
  }

  private lazy val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Opiskelijavuositiedot" - {
    val oid = "1.2.246.562.15.123456"

    "raportti sisältää oikeat tiedot" in {
      val result = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"), t)

      val aarnenOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammattilainen.oid).oid.get
      val aarnenRivi = result.find(_.opiskeluoikeusOid == aarnenOpiskeluoikeusOid)
      aarnenRivi shouldBe defined
      val rivi = aarnenRivi.get

      rivi.suorituksenTyyppi should equal("ammatillinentutkinto")
      rivi.koulutusmoduulit should equal("361902")
      rivi.päätasonSuoritustenNimet should equal("Luonto- ja ympäristöalan perustutkinto")
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

    "raportti sisältää oikeat päätason suorituksen tiedot uudella ja vanhalla perusteella" in {
      val result = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2022-08-01"), LocalDate.parse("2022-08-01"), t)

      val ajoneuvonOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ajoneuvoalanOpiskelija.oid).oid.get
      val ajoneuvoRivi = result.find(_.opiskeluoikeusOid == ajoneuvonOpiskeluoikeusOid)
      ajoneuvoRivi shouldBe defined
      val riviUusiOps = ajoneuvoRivi.get

      riviUusiOps.suorituksenTyyppi should equal("ammatillinentutkinto")
      riviUusiOps.koulutusmoduulit should equal("351301")
      riviUusiOps.päätasonSuoritustenNimet should equal("Ajoneuvoalan perustutkinto")
      riviUusiOps.päätasonSuorituksenSuoritustapa should equal("Reformin mukainen näyttö")

      val eeronOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid).oid.get
      val eeroRivi = result.find(_.opiskeluoikeusOid == eeronOpiskeluoikeusOid)
      eeroRivi shouldBe defined
      val riviVanhaOps = eeroRivi.get
      riviVanhaOps.suorituksenTyyppi should equal("ammatillinentutkinto")
      riviVanhaOps.koulutusmoduulit should equal("351301")
      riviVanhaOps.päätasonSuoritustenNimet should equal("Autoalan perustutkinto")
      riviVanhaOps.päätasonSuorituksenSuoritustapa should equal("Ammatillinen perustutkinto")
    }

    "raportti sisältää maksuttomuuden tiedot" in {
      val result = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2028-01-01"), LocalDate.parse("2028-12-31"), t)

      val opiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid).oid.get
      val riviOpt = result.find(_.opiskeluoikeusOid == opiskeluoikeusOid)
      riviOpt shouldBe defined
      val rivi = riviOpt.get

      rivi.maksuttomuus shouldBe Some("2027-02-01 – 2028-01-31, 2028-02-01 – ")
      rivi.oikeuttaMaksuttomuuteenPidennetty shouldBe Some("2026-02-01 – 2027-01-31, 2027-02-01 – 2028-01-31, 2028-02-01 – 2028-03-01")
    }

    "ostettu" in {
      val markkasenOpiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.markkanen.oid).oid.get
      val rivi = AmmatillinenOpiskalijavuositiedotRaportti.buildRaportti(raportointiDatabase, MockOrganisaatiot.omnia, LocalDate.parse("2000-01-01"), LocalDate.parse("2020-01-02"), t)
        .find(_.opiskeluoikeusOid == markkasenOpiskeluoikeusOid)
        .get
      rivi.ostettu should equal(true)
    }

    "opiskelijavuoteen kuuluvat ja muut lomat lasketaan oikein" - {
      "lasna-tilaa ei lasketa lomaksi" in {
        AmmatillinenRaporttiUtils.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-05-31"), "lasna", Date.valueOf("2016-01-15"))
        )) should equal((0, 0))
      }
      "lyhyt loma (alle 28 pv) lasketaan kokonaan opiskelijavuoteen" in {
        AmmatillinenRaporttiUtils.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-15"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-15")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-05"), "loma", Date.valueOf("2016-02-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-06"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-02-06"))
        )) should equal((5, 0))
      }
      "pitkästä lomasta lasketaan 28 pv opiskelijavuoteen, loput muihin" in {
        AmmatillinenRaporttiUtils.lomaPäivät(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-12-31"), "loma", Date.valueOf("2016-01-01"))
        )) should equal((28, 366 - 28))
      }
      "jos loma on alkanut ennen tätä aikajaksoa" - {
        "jos päiviä on tarpeeksi jäljellä, koko jakso lasketaan opiskelijavuoteen" in {
          AmmatillinenRaporttiUtils.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2016-01-25"))
          )) should equal((14, 0))
        }
        "jos päiviä on jäljellä jonkin verran, osa jaksosta lasketaan opiskelijavuoteen" in {
          AmmatillinenRaporttiUtils.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-03-31"), "loma", Date.valueOf("2016-01-31"))
          )) should equal((27, 33))
        }
        "jos päiviä ei ole jäljellä yhtään, koko jakso lasketaan muihin lomiin" in {
          AmmatillinenRaporttiUtils.lomaPäivät(Seq(
            ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-14"), "loma", Date.valueOf("2015-12-01"))
          )) should equal((0, 14))
        }
      }
    }

    "opiskelijavuosikertymä lasketaan oikein" - {

      "läsnäolopäivät lasketaan mukaan" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"))
        )) should equal(31)
      }

      "osa-aikaisuus huomioidaan" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50)
        )) should equal(15.5)
      }

      "valmistumispäivä lasketaan mukaan" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(32)
      }

      "eroamispäivää ei lasketa mukaan" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "eronnut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true)
        )) should equal(31)
      }

      "lomapäivät lasketaan mukaan" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01")),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-10"), "loma", Date.valueOf("2016-02-01"))
        )) should equal(41)
      }

      "valmistumispäivä lasketaan aina 100% läsnäolopäivänä, vaikka opinnot olisivat olleet osa-aikaisia" in {
        AmmatillinenRaporttiUtils.opiskelijavuosikertymä(Seq(
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-01-01"), Date.valueOf("2016-01-31"), "lasna", Date.valueOf("2016-01-01"), osaAikaisuus = 50),
          ROpiskeluoikeusAikajaksoRow(oid, Date.valueOf("2016-02-01"), Date.valueOf("2016-02-01"), "valmistunut", Date.valueOf("2016-02-01"), opiskeluoikeusPäättynyt = true, osaAikaisuus = 50)
        )) should equal(16.5)
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/ammatillinenopiskelijavuositiedot", expectedRaporttiNimi = AmmatillinenOpiskelijavuositiedot.toString, expectedFileNamePrefix = "opiskelijavuositiedot")
    }

    "raportin lataaminen toimii eri lokalisaatiolla (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/ammatillinenopiskelijavuositiedot", expectedRaporttiNimi = AmmatillinenOpiskelijavuositiedot.toString, expectedFileNamePrefix = "uppgifter om studerandeår", lang = "sv")
    }

    "käyttöoikeudet" - {
      "raportin lataaminen vaatii käyttöoikeudet organisaatioon" in {
        authGet(s"api/raportit/ammatillinenopiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = omniaTallentaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Käyttäjällä ei oikeuksia annettuun organisaatioon (esimerkiksi oppilaitokseen)."))
        }
      }

      "raportin lataaminen ei ole sallittu viranomais-käyttäjille (globaali-luku)" in {
        authGet(s"api/raportit/ammatillinenopiskelijavuositiedot?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31&password=dummy", user = viranomainenGlobaaliKatselija) {
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
