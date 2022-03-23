package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{PutOpiskeluoikeusTestMethods, TestMethodsLukio}
import fi.oph.koski.documentation.ExampleData.{suomenKieli}
import fi.oph.koski.documentation.ExamplesLukio2019.{lops2019perusteenDiaarinumero, lukionOppimäärä2019}
import fi.oph.koski.documentation.{Lukio2019ExampleData}
import fi.oph.koski.documentation.Lukio2019ExampleData.{moduulinSuoritusOppiaineissa, muuModuuliOppiaineissa, numeerinenArviointi, numeerinenLukionOppiaineenArviointi, oppiaineenSuoritus, paikallinenOpintojakso, paikallisenOpintojaksonSuoritus}
import fi.oph.koski.documentation.LukioExampleData.nuortenOpetussuunnitelma
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio.lops2021.{Lukio2019AineopinnotOpiskeluoikeudenUlkopuolisetRow, Lukio2019ModuulinRahoitusmuotoRow, Lukio2019OpintopistekertymaAineopiskelijaRow, Lukio2019OppiaineEriVuonnaKorotetutOpintopisteetRow, LukioOpintopistekertymaOppimaaraRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{LocalizedString, LukionOpiskeluoikeus, LukionOppiaineenSuoritus2019, LukionOppiaineidenOppimäärienSuoritus2019, LukionOppiaineidenOppimäärät2019, LukionOppimääränSuoritus2019, Oppija, OsaamisenTunnustaminen, PaikallinenKoodi, PaikallinenLukionOppiaine2019}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}
import java.time.LocalDate

object TestData {
  lazy val tunnustus = Some(OsaamisenTunnustaminen(
    osaaminen = None,
    selite = LocalizedString.finnish("osaamisen tunnustaminen")
  ))

  lazy val oppiaineSuoritukset: List[LukionOppiaineenSuoritus2019] = List(
    oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1").copy(laajuus = Lukio2019ExampleData.laajuus(2))).copy(arviointi = numeerinenArviointi(8, date(2000, 1, 1))),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2").
        copy(laajuus = Lukio2019ExampleData.laajuus(2))).copy(arviointi = numeerinenArviointi(8, date(2000, 1, 1)), tunnustettu = tunnustus),
    ))),
    oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", LocalizedString.finnish("Tanssi ja liike")), LocalizedString.finnish("Tanssi ja liike"), pakollinen = false)).copy(arviointi = numeerinenLukionOppiaineenArviointi(8)).copy(osasuoritukset = Some(List(
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne").copy(laajuus = Lukio2019ExampleData.laajuus(1), pakollinen = false)).copy(arviointi = numeerinenArviointi(10, date(2000, 1, 1))),
    )))
  )

  lazy val oppiaineidenOppimäärienSuoritus = LukionOppiaineidenOppimäärienSuoritus2019(
    koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019perusteenDiaarinumero),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiaineSuoritukset)
  )

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä2019,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    vahvistus = None,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiaineSuoritukset),
  )
}

class Lukio2019OpintopistekertymaRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[LukionOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = TestMethodsLukio.lukionOpiskeluoikeus

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val opiskelija = KoskiSpecificMockOppijat.teija

    val oppimäärä = defaultOpiskeluoikeus.copy(
      suoritukset = List(TestData.oppimääränSuoritus),
    )
    val aine = defaultOpiskeluoikeus.copy(
      suoritukset = List(TestData.oppiaineidenOppimäärienSuoritus),
    )
    putOppija(Oppija(opiskelija, List(oppimäärä, aine))) {
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
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukio2019opintopistekertymat&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2000-01-01&loppu=2001-01-01&lang=fi")))
    }
  }

  "Excel välilehtien sarakkeet, valitaan vain ne kurssit joiden arviointipäivä on aikavälin sisällä" - {
    "Oppimäärän välilehti (lukio ops 2019)" - {
      "OppilaitosOid" in {
        jyväskylänOppimäärä.oppilaitosOid shouldBe(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
      "Suoritettuja" in {
        jyväskylänOppimäärä.suoritettujaOpintopisteita shouldBe(3)
      }
      "Tunnustettuja" in {
        jyväskylänOppimäärä.tunnustettujaOpintopisteita shouldBe(2)
      }
      "Kursseja yhteensä" in {
        jyväskylänOppimäärä.opintopisteitaYhteensa shouldBe(5)
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
        jyväskylänAineopiskelijat.opintopisteitaYhteensa shouldBe(5)
      }
      "Suoritettuja" in {
        jyväskylänAineopiskelijat.suoritettujaOpintopisteita shouldBe(3)
      }
      "Tunnustettuja" in {
        jyväskylänAineopiskelijat.tunnustettujaOpintopisteita shouldBe(2)
      }
      "Tunnustettuja rahoituksen piirissä" in {
        jyväskylänAineopiskelijat.tunnustettujaOpintopisteita_rahoituksenPiirissa shouldBe(0)
      }
      "Pakollisia tai valtakunnallinen ja syventava" in {
        jyväskylänAineopiskelijat.pakollisia_tai_valtakunnallisiaSyventavia shouldBe(0)
      }
      "Pakollisia" in {
        jyväskylänAineopiskelijat.pakollisiaOpintopisteita shouldBe(0)
      }
      "Valtakunnallisia syventavia" in {
        jyväskylänAineopiskelijat.valtakunnallisestiSyventaviaOpintopisteita shouldBe(0)
      }
      "Suoritettuja pakollisia ja suoritettuja valtakunnallisia syventavia" in {
        jyväskylänAineopiskelijat.suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia shouldBe(0)
      }
      "Suoritettuja pakollisia" in {
        jyväskylänAineopiskelijat.suoritettujaPakollisiaOpintopisteita shouldBe(0)
      }
      "Suoritettuja valtakunnallisia syventavia" in {
        jyväskylänAineopiskelijat.suoritettujaValtakunnallisiaSyventaviaOpintopisteita shouldBe(0)
      }
      "Tunnustettuja pakollisia ja tunnustettuja valtakunnallisia syventavia" in {
        jyväskylänAineopiskelijat.tunnustettujaPakollisia_ja_tunnustettujaValtakunnallisiaSyventavia shouldBe(0)
      }
      "Tunnustettuja pakollisia" in {
        jyväskylänAineopiskelijat.tunnustettujaPakollisiaOpintopisteita shouldBe(0)
      }
      "Tunnustettuja valtakunnallisia syventavia" in {
        jyväskylänAineopiskelijat.tunnustettujaValtakunnallisiaSyventaviaOpintopisteita shouldBe(0)
      }
      "Tunnustettuja rahoituksen piirissä pakollisista ja valtakunnallisesti syventävistä kursseista" in {
        jyväskylänAineopiskelijat.tunnustettujaRahoituksenPiirissa_pakollisia_ja_valtakunnallisiaSyventavia shouldBe(0)
      }
      "Tunnustettuja rahoituksen piirissa pakollisia" in {
        jyväskylänAineopiskelijat.tunnustettuja_rahoituksenPiirissa_pakollisia shouldBe(0)
      }
      "Tunnustettuja rahoituksen piirissa valtakunnallisesti syventavia" in {
        jyväskylänAineopiskelijat.tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva shouldBe(0)
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - muuta kautta rahoitetut" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut shouldBe 0
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - rahoitusmuoto ei tiedossa" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_rahoitusmuotoEiTiedossa shouldBe 0
      }
      "Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit – arviointipäivä ei opiskeluoikeuden sisällä" in {
        jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_eiOpiskeluoikeudenSisalla shouldBe 0
      }
      "Suoritetut kurssit, joiden arviointia nostettu myöhempänä vuonna kuin jona ensimmäinen arviointi annettu" in {
        jyväskylänAineopiskelijat.eriVuonnaKorotettujaOpintopisteita shouldBe 0
      }
    }
    "Muuta kautta rahoitetuttujen välilehti" - {
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        jyväskylänMuutaKauttaRahoitetut.length shouldBe jyväskylänAineopiskelijat.suoritetutTaiRahoitetut_muutaKauttaRahoitetut
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
      "Listan pituus sama kuin aineopiskelijoiden välilehdellä oleva laskuri" in {
        jyväskylänOpiskeluoikeudenEriVuonnaArvioidut.length shouldBe 0
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
