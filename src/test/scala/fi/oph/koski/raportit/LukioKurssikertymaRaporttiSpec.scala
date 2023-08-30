package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.misc.{PutOpiskeluoikeusTestMethods, TestMethodsLukio}
import fi.oph.koski.documentation.ExamplesLukio.{aikuistenOpsinPerusteet2015, aineopiskelija}
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData, YleissivistavakoulutusExampleData}
import fi.oph.koski.documentation.ExampleData.suomenKieli
import fi.oph.koski.documentation.LukioExampleData.{kurssisuoritus, lukionOppiaine, numeerinenArviointi, opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt, valtakunnallinenKurssi, valtakunnallinenVanhanOpsinKurssi}
import fi.oph.koski.fixture.LukioKurssikertymaRaporttiFixtures
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio.{LukioKurssikertymaAineopiskelijaRow, LukioKurssikertymaOppimaaraRow, LukioKurssinRahoitusmuotoRow, LukioOppiaineEriVuonnaKorotetutKurssitRow, LukioOppiaineOpiskeluoikeudenUlkopuolisetRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{LukionOpiskeluoikeudenTila, LukionOpiskeluoikeus, LukionOpiskeluoikeusjakso, LukionOppiaineenOppimääränSuoritus2015, LukionPäätasonSuoritus, Oppija}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class LukioKurssikertymaRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[LukionOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = TestMethodsLukio.lukionOpiskeluoikeus

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    val aikuisOpiskelija = KoskiSpecificMockOppijat.aikuisOpiskelija
    val opiskeluOikeusAikuistenOpsilla = aineopiskelija.copy(
      lähdejärjestelmänId = Some(AmmatillinenExampleData.primusLähdejärjestelmäId("l-0303032")),
      oppilaitos = Some(YleissivistavakoulutusExampleData.ressunLukio),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku, tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )
      ),
      suoritukset = List(
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("HI", diaarinumero = Some(aikuistenOpsinPerusteet2015)),
          suorituskieli = suomenKieli,
          toimipiste = YleissivistavakoulutusExampleData.ressunLukio,
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("HI1")).copy(arviointi = numeerinenArviointi(7, päivä = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku)),
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("HI2")).copy(arviointi = numeerinenArviointi(7, päivä = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku))
          ))
        )
      )
    )

    putOppija(Oppija(aikuisOpiskelija, List(opiskeluOikeusAikuistenOpsilla))) {
      verifyResponseStatusOk()
    }

    reloadRaportointikanta
  }

  override def defaultUser = MockUsers.helsinginKaupunkiPalvelukäyttäjä

  "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
    AuditLogTester.clearMessages
    authGet(s"api/raportit/lukiokurssikertymat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01&lang=fi&password=salasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukion_kurssikertymat_20180101-20180101.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiokurssikertymat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01&lang=fi")))
    }
  }

  "Raportin lataaminen eri lokalisaatiolla onnistuu ja tuottaa auditlogin" in {
    AuditLogTester.clearMessages
    authGet(s"api/raportit/lukiokurssikertymat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01&lang=sv&password=salasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="gymnasiets_antal_kurser_20180101-20180101.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiokurssikertymat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01&lang=sv")))
    }
  }

  "Excel välilehtien sarakkeet, valitaan vain ne kurssit joiden arviointipäivä on aikavälin sisällä" - {
    "Oppimäärän välilehti (lukio ops 2015)" - {
      "OppilaitosOid" in {
        helsinkiOppimaara.oppilaitosOid shouldBe(MockOrganisaatiot.helsinginMedialukio)
      }
      "Suoritettuja" in {
        helsinkiOppimaara.suoritettujaKursseja shouldBe(2)
      }
      "Tunnustettuja" in {
        helsinkiOppimaara.tunnustettujaKursseja shouldBe(4)
      }
      "Kursseja yhteensä" in {
        helsinkiOppimaara.kurssejaYhteensa shouldBe(6)
      }
      "Tunnustettuja rahoituksen piirissa" in {
        helsinkiOppimaara.tunnustettujaKursseja_rahoituksenPiirissa shouldBe(2)
      }
    }
    "Aineopiskelijoiden välilehti" - {
      "Oppilaitoksen Oid" in {
        ressunAineopiskelijat.oppilaitosOid shouldBe(MockOrganisaatiot.ressunLukio)
      }
      "Yhteensä" in {
        ressunAineopiskelijat.kurssejaYhteensa shouldBe(13)
      }
      "Suoritettuja" in {
        ressunAineopiskelijat.suoritettujaKursseja shouldBe(7)
      }
      "Tunnustettuja" in {
        ressunAineopiskelijat.tunnustettujaKursseja shouldBe(6)
      }
      "Tunnustettuja rahoituksen piirissä" in {
        ressunAineopiskelijat.tunnustettujaKursseja_rahoituksenPiirissa shouldBe(3)
      }
      "Pakollisia tai valtakunnallinen ja syventava" in {
        ressunAineopiskelijat.pakollisia_tai_valtakunnallisiaSyventavia shouldBe(10)
      }
      "Pakollisia" in {
        ressunAineopiskelijat.pakollisiaKursseja shouldBe(7)
      }
      "Valtakunnallisia syventavia" in {
        ressunAineopiskelijat.valtakunnallisestiSyventaviaKursseja shouldBe(3)
      }
      "Suoritettuja pakollisia ja suoritettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia shouldBe(6)
      }
      "Suoritettuja pakollisia" in {
        ressunAineopiskelijat.suoritettujaPakollisiaKursseja shouldBe(5)
      }
      "Suoritettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.suoritettujaValtakunnallisiaSyventaviaKursseja shouldBe(1)
      }
      "Tunnustettuja pakollisia ja tunnustettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisiaSyventavia shouldBe(4)
      }
      "Tunnustettuja pakollisia" in {
        ressunAineopiskelijat.tunnustettujaPakollisiaKursseja shouldBe(2)
      }
      "Tunnustettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.tunnustettujaValtakunnallisiaSyventaviaKursseja shouldBe(2)
      }
      "Tunnustettuja rahoituksen piirissä pakollisista ja valtakunnallisesti syventävistä kursseista" in {
        ressunAineopiskelijat.tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisiaSyventavia shouldBe(2)
      }
      "Tunnustettuja rahoituksen piirissa pakollisia" in {
        ressunAineopiskelijat.tunnustettuja_rahoituksenPiirissa_pakollisia shouldBe(1)
      }
      "Tunnustettuja rahoituksen piirissa valtakunnallisesti syventavia" in {
        ressunAineopiskelijat.tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva shouldBe(1)
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - muuta kautta rahoitetut" in {
        ressunAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut shouldBe 1
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - rahoitusmuoto ei tiedossa" in {
        ressunAineopiskelijat.suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa shouldBe 0
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit – arviointipäivä ei opiskeluoikeuden sisällä" in {
        ressunAineopiskelijat.suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla shouldBe 1
      }
      "Suoritetut kurssit, joiden arviointia nostettu myöhempänä vuonna kuin jona ensimmäinen arviointi annettu" in {
        ressunAineopiskelijat.eriVuonnaKorotettujaKursseja shouldBe 1
      }
    }
    "Aikuisaineopiskelijan välilehti" - {
      "Oppilaitoksen Oid" in {
        ressunAikuisAineopiskelijat.oppilaitosOid shouldBe(MockOrganisaatiot.ressunLukio)
      }
      "Yhteensä" in {
        ressunAikuisAineopiskelijat.kurssejaYhteensa shouldBe(2)
      }
      "Suoritettuja" in {
        ressunAikuisAineopiskelijat.suoritettujaKursseja shouldBe(2)
      }
    }
    "Muuta kautta rahoitetuttujen välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        ressunMuutaKauttaRahoitetut.length shouldBe ressunAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut
      }
    }
    "Rahoitusmuoto ei tiedossa -välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        ressunRahoitusmuotoEiTiedossa.length shouldBe ressunAineopiskelijat.suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa
      }
    }
    "Arviointipäivä opiskeluoikeuden ulkopuolella -välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        ressunOpiskeluoikeudenUlkopuolisetArvionnit.length shouldBe 1
      }
    }
    "Eri vuonna korotetut kurssit -välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        ressunOpiskeluoikeudenEriVuonnaArvioidut.length shouldBe 1
      }
    }
  }

  lazy val raportti = loadRaportti

  lazy val helsinkiOppimaara = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-oppimäärä-sheet-name") => d.rows.collect {
      case r: LukioKurssikertymaOppimaaraRow => r
    }
  }.get.find(_.oppilaitos == "Helsingin medialukio").get

  lazy val ressunAineopiskelijat = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-aineopiskelijat-sheet-name") => d.rows.collect {
      case r: LukioKurssikertymaAineopiskelijaRow => r
    }
  }.get.find(_.oppilaitos == "Ressun lukio").get

  lazy val ressunAikuisAineopiskelijat = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-aineopiskelijat-aikuisten-ops-sheet-name") => d.rows.collect {
      case r: LukioKurssikertymaAineopiskelijaRow => r
    }
  }.get.find(_.oppilaitos == "Ressun lukio").get

  lazy val ressunMuutaKauttaRahoitetut: Seq[LukioKurssinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-muutakauttarah-sheet-name") => d.rows.collect {
      case r: LukioKurssinRahoitusmuotoRow => r
    }
  }.get

  lazy val ressunRahoitusmuotoEiTiedossa: Seq[LukioKurssinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-eirahoitusmuotoa-sheet-name") => d.rows.collect {
      case r: LukioKurssinRahoitusmuotoRow => r
    }
  }.get

  lazy val ressunOpiskeluoikeudenUlkopuolisetArvionnit: Seq[LukioOppiaineOpiskeluoikeudenUlkopuolisetRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-opiskeluoikeudenulkop-sheet-name") => d.rows.collect {
      case r: LukioOppiaineOpiskeluoikeudenUlkopuolisetRow => r
    }
  }.get

  lazy val ressunOpiskeluoikeudenEriVuonnaArvioidut: Seq[LukioOppiaineEriVuonnaKorotetutKurssitRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-erivuonnakorotetutkurssit-sheet-name") => d.rows.collect {
      case r: LukioOppiaineEriVuonnaKorotetutKurssitRow => r
    }
  }.get

  private def loadRaportti = {
    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = MockOrganisaatiot.helsinginKaupunki,
      downloadToken = None,
      password = "foobar",
      alku = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku,
      loppu = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoLoppu,
      lang = "fi"
    )

    new RaportitService(KoskiApplicationForTests).lukioKoulutuksenKurssikertyma(request, t).sheets
  }
}
