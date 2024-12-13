package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.misc.{PutOpiskeluoikeusTestMethods, TestMethodsLukio}
import fi.oph.koski.documentation.ExamplesLukio2019.lops2019AikuistenPerusteenDiaarinumero
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.LukioExampleData.{aikuistenOpetussuunnitelma, opiskeluoikeusAktiivinen}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019AineopinnotOpiskeluoikeudenUlkopuolisetRow, Lukio2019ModuulinRahoitusmuotoRow, Lukio2019OpintopistekertymaAineopiskelijaRow, Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow, LukioOpintopistekertymaOppimaaraRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{LukionOpiskeluoikeudenTila, LukionOpiskeluoikeus, LukionOpiskeluoikeusjakso, LukionOppiaineidenOppimäärät2019, Oppija}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}
import java.time.LocalDate

class Lukio2019OpintopistekertymaRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[LukionOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = TestMethodsLukio.lukionOpiskeluoikeus

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val opiskelijaSuppea = KoskiSpecificMockOppijat.teija
    val opiskelijaRahoituspuljaus = KoskiSpecificMockOppijat.eero
    val aikuisOpiskelija = KoskiSpecificMockOppijat.aikuisOpiskelija
    val aikuisOpiskelijaMuuRahoitus = KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus
    val aikuisOpiskelijaVieraskielinen = KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinen

    // Aineopinnot

    val aineopinnotAikuistenOps = defaultOpiskeluoikeus.copy(
      suoritukset = List(Lukio2019RaaportitTestData.oppiaineidenOppimäärienSuoritus.copy(
        oppimäärä = aikuistenOpetussuunnitelma,
        koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019AikuistenPerusteenDiaarinumero)
      ))
    )

    val aineopiskelijaAikuistenOps = Oppija(aikuisOpiskelija, List(aineopinnotAikuistenOps))

    putOppija(aineopiskelijaAikuistenOps) {
      verifyResponseStatusOk()
    }

    // Lukion oppimäärä

    val oppimääränSuoritusOo = defaultOpiskeluoikeus.copy(
      suoritukset = List(Lukio2019RaaportitTestData.oppimääränSuoritus),
    )

    val aineopintojenOppimääräOo = defaultOpiskeluoikeus.copy(
      suoritukset = List(Lukio2019RaaportitTestData.oppiaineidenOppimäärienSuoritus),
    )

    val molemmatOppimäärät = Oppija(opiskelijaSuppea, List(oppimääränSuoritusOo, aineopintojenOppimääräOo))

    putOppija(molemmatOppimäärät) {
      verifyResponseStatusOk()
    }

    // Muuta kautta rahoitetut

    val opiskeluoikeusjaksoMuutaKauttaRahoitettu = LukionOpiskeluoikeusjakso(alku = date(2000, 1, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.muutaKauttaRahoitettu))

    val aineopiskeluMuutaKauttaRahoitettu = aineopintojenOppimääräOo.copy(
      tila = LukionOpiskeluoikeudenTila(List(opiskeluoikeusjaksoMuutaKauttaRahoitettu))
    )

    val muutaKauttaRahoitettuAineopiskelija = Oppija(opiskelijaRahoituspuljaus, opiskeluoikeudet = List(aineopiskeluMuutaKauttaRahoitettu))
    putOppija(muutaKauttaRahoitettuAineopiskelija) {
      verifyResponseStatusOk()
    }

    val aineopiskeluAikuistenOpsMuutaKauttaRahoitettu = aineopinnotAikuistenOps.copy(
      tila = LukionOpiskeluoikeudenTila(List(opiskeluoikeusjaksoMuutaKauttaRahoitettu))
    )

    val muutaKauttaRahoitettuAineopiskelijaAikuistenOps = Oppija(aikuisOpiskelijaVieraskielinen, List(aineopiskeluAikuistenOpsMuutaKauttaRahoitettu))
    putOppija(muutaKauttaRahoitettuAineopiskelijaAikuistenOps) {
      verifyResponseStatusOk()
    }

    val muutaKauttaRahoitettuAineopiskelijaAikuistenOps2 = Oppija(aikuisOpiskelijaMuuRahoitus, List(aineopiskeluAikuistenOpsMuutaKauttaRahoitettu))
    putOppija(muutaKauttaRahoitettuAineopiskelijaAikuistenOps2) {
      verifyResponseStatusOk()
    }

    reloadRaportointikanta
  }

  "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
    AuditLogTester.clearMessages
    authGet(s"api/raportit/lukio2019opintopistekertymat?oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2000-01-01&loppu=2001-01-01&lang=fi&password=salasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukio2019_opintopistekertymat_20000101-20010101.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukio2019opintopistekertymat&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2000-01-01&loppu=2001-01-01&lang=fi")))
    }
  }

  "Excel välilehtien sarakkeet, valitaan vain ne kurssit joiden arviointipäivä on aikavälin sisällä" - {
    "Oppimäärän välilehti (lukio ops 2019)" - {
      "OppilaitosOid" in {
        jyväskylänOppimäärä.oppilaitosOid shouldBe(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
      "Suoritettuja" in {
        jyväskylänOppimäärä.suoritettujaOpintopisteita shouldBe(4)
      }
      "Tunnustettuja" in {
        jyväskylänOppimäärä.tunnustettujaOpintopisteita shouldBe(4)
      }
      "Kursseja yhteensä" in {
        jyväskylänOppimäärä.opintopisteitaYhteensa shouldBe(8)
      }
      "Tunnustettuja rahoituksen piirissa" in {
        jyväskylänOppimäärä.tunnustettujaOpintopisteita_rahoituksenPiirissa shouldBe(0)
      }
    }
    "Aineopiskelijoiden välilehti" - {
      "Oppilaitoksen Oid" in {
        jyväskylänAineopiskelijat.oppilaitosOid shouldBe(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
      "Yhteensä" in {
        jyväskylänAineopiskelijat.opintopisteitaYhteensa shouldBe(16)
      }
      "Suoritettuja" in {
        jyväskylänAineopiskelijat.suoritettujaOpintopisteita shouldBe(8)
      }
      "Tunnustettuja" in {
        jyväskylänAineopiskelijat.tunnustettujaOpintopisteita shouldBe(8)
      }
      "Tunnustettuja rahoituksen piirissä" in {
        jyväskylänAineopiskelijat.tunnustettujaOpintopisteita_rahoituksenPiirissa shouldBe(0)
      }
      "Pakollisia tai valtakunnallinen" in {
        jyväskylänAineopiskelijat.pakollisia_tai_valtakunnallisia shouldBe(12)
      }
      "Pakollisia" in {
        jyväskylänAineopiskelijat.pakollisiaOpintopisteita shouldBe(12)
      }
      "Valtakunnallisia" in {
        jyväskylänAineopiskelijat.valtakunnallisiaOpintopisteita shouldBe(12)
      }
      "Suoritettuja pakollisia ja suoritettuja valtakunnallisia" in {
        jyväskylänAineopiskelijat.suoritettujaPakollisia_ja_suoritettujaValtakunnallisia shouldBe(4)
      }
      "Suoritettuja pakollisia" in {
        jyväskylänAineopiskelijat.suoritettujaPakollisiaOpintopisteita shouldBe(4)
      }
      "Suoritettuja valtakunnallisia" in {
        jyväskylänAineopiskelijat.suoritettujaValtakunnallisiaOpintopisteita shouldBe(4)
      }
      "Tunnustettuja pakollisia ja tunnustettuja valtakunnallisia" in {
        jyväskylänAineopiskelijat.tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisia shouldBe(8)
      }
      "Tunnustettuja pakollisia" in {
        jyväskylänAineopiskelijat.tunnustettujaPakollisiaOpintopisteita shouldBe(8)
      }
      "Tunnustettuja valtakunnallisia" in {
        jyväskylänAineopiskelijat.tunnustettujaValtakunnallisiaOpintopisteita shouldBe(8)
      }
      "Tunnustettuja rahoituksen piirissä pakollisista ja valtakunnallisesti kursseista" in {
        jyväskylänAineopiskelijat.tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisia shouldBe(0)
      }
      "Tunnustettuja rahoituksen piirissa pakollisia" in {
        jyväskylänAineopiskelijat.tunnustettuja_rahoituksenPiirissa_pakollisia shouldBe(0)
      }
      "Tunnustettuja rahoituksen piirissa valtakunnallisia" in {
        jyväskylänAineopiskelijat.tunnustettuja_rahoituksenPiirissa_valtakunnallisia shouldBe(0)
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - muuta kautta rahoitetut" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut shouldBe 2
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - rahoitusmuoto ei tiedossa" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa shouldBe 0
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit – arviointipäivä ei opiskeluoikeuden sisällä" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla shouldBe 0
      }
      "Suoritetut kurssit, joiden arviointia nostettu myöhempänä vuonna kuin jona ensimmäinen arviointi annettu" in {
        jyväskylänAineopiskelijat.eriVuonnaKorotettujaOpintopisteita shouldBe 4
      }
    }
    "Aikuisaineopiskelijan välilehti" - {
      "Oppilaitoksen Oid" in {
        jyväskylänAikuisAineopiskelijat.oppilaitosOid shouldBe(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
      "Yhteensä" in {
        jyväskylänAikuisAineopiskelijat.opintopisteitaYhteensa shouldBe(24)
      }
      "Suoritettuja" in {
        jyväskylänAikuisAineopiskelijat.suoritettujaOpintopisteita shouldBe(12)
      }
      "Tunnustettuja" in {
        jyväskylänAikuisAineopiskelijat.tunnustettujaOpintopisteita shouldBe(12)
      }
      "Tunnustettuja rahoituksen piirissä" in {
        jyväskylänAikuisAineopiskelijat.tunnustettujaOpintopisteita_rahoituksenPiirissa shouldBe(0)
      }
      "Muuta kautta rahoitettuja" in {
        jyväskylänAikuisAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut shouldBe(4)
      }
    }
    "Muuta kautta rahoitetuttujen välilehti" - {
      "Listan pituus" in {
        jyväskylänMuutaKauttaRahoitetut.length shouldBe 6
      }
    }
    "Rahoitusmuoto ei tiedossa -välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        jyväskylänRahoitusmuotoEiTiedossa.length shouldBe jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa
      }
    }
    "Arviointipäivä opiskeluoikeuden ulkopuolella -välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        jyväskylänOpiskeluoikeudenUlkopuolisetArvionnit.length shouldBe 0
      }
    }
    "Eri vuonna korotetut kurssit -välilehti" - {
      "Listan pituus" in {
        jyväskylänOpiskeluoikeudenEriVuonnaArvioidut.length shouldBe 5
      }
    }
  }

  lazy val raportti = loadRaportti

  lazy val jyväskylänOppimäärä = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-oppimäärä-sheet-name") => d.rows.collect {
      case r: LukioOpintopistekertymaOppimaaraRow => r
    }
  }.get.find(_.oppilaitos == "Jyväskylän normaalikoulu").get

  lazy val jyväskylänAineopiskelijat = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-aineopiskelijat-sheet-name") => d.rows.collect {
      case r: Lukio2019OpintopistekertymaAineopiskelijaRow => r
    }
  }.get.find(_.oppilaitos == "Jyväskylän normaalikoulu").get

  lazy val jyväskylänAikuisAineopiskelijat = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-aineopiskelijat-aikuisten-ops-sheet-name") => d.rows.collect {
      case r: Lukio2019OpintopistekertymaAineopiskelijaRow => r
    }
  }.get.find(_.oppilaitos == "Jyväskylän normaalikoulu").get

  lazy val jyväskylänMuutaKauttaRahoitetut: Seq[Lukio2019ModuulinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-muutakauttarah-sheet-name") => d.rows.collect {
      case r: Lukio2019ModuulinRahoitusmuotoRow => r
    }
  }.get

  lazy val jyväskylänRahoitusmuotoEiTiedossa: Seq[Lukio2019ModuulinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-eirahoitusmuotoa-sheet-name") => d.rows.collect {
      case r: Lukio2019ModuulinRahoitusmuotoRow => r
    }
  }.get

  lazy val jyväskylänOpiskeluoikeudenUlkopuolisetArvionnit: Seq[Lukio2019AineopinnotOpiskeluoikeudenUlkopuolisetRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-opiskeluoikeudenulkop-sheet-name") => d.rows.collect {
      case r: Lukio2019AineopinnotOpiskeluoikeudenUlkopuolisetRow => r
    }
  }.get

  lazy val jyväskylänOpiskeluoikeudenEriVuonnaArvioidut: Seq[Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow] = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-erivuonnakorotetutopintopisteet-sheet-name") => d.rows.collect {
      case r: Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow => r
    }
  }.get

  private def loadRaportti = {
    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu,
      downloadToken = None,
      password = "foobar",
      alku = LocalDate.of(2000, 1, 1),
      loppu = LocalDate.of(2001, 1, 1),
      lang = "fi"
    )

    new RaportitService(KoskiApplicationForTests).lukio2019KoulutuksenOpintopistekertyma(request, t).sheets
  }
}
