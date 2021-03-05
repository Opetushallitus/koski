package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioKurssikertymaRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class LukioKurssikertymaRaporttiSpec extends FreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll {

  override def beforeAll = {
    reloadRaportointikanta
  }

  override def defaultUser = MockUsers.helsinginKaupunkiPalvelukäyttäjä

  "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
    AuditLogTester.clearMessages
    authGet(s"api/raportit/lukiokurssikertymat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01&password=salasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukion_kurssikertymat_20180101-20180101.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiokurssikertymat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&alku=2018-01-01&loppu=2018-01-01")))
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
  }

  lazy val raportti = loadRaportti

  lazy val helsinkiOppimaara = raportti.collectFirst {
    case d: DataSheet if d.title == LukioOppimaaranKussikertymat.sheetTitle => d.rows.collect {
      case r: LukioKurssikertymaOppimaaraRow => r
    }
  }.get.find(_.oppilaitos == "Helsingin medialukio").get

  lazy val ressunAineopiskelijat = raportti.collectFirst {
    case d: DataSheet if d.title == LukioOppiaineenOppimaaranKurssikertymat.sheetTitle => d.rows.collect {
      case r: LukioKurssikertymaAineopiskelijaRow => r
    }
  }.get.find(_.oppilaitos == "Ressun lukio").get

  lazy val ressunMuutaKauttaRahoitetut: Seq[LukioKurssinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == LukioMuutaKauttaRahoitetut.sheetTitle => d.rows.collect {
      case r: LukioKurssinRahoitusmuotoRow => r
    }
  }.get

  lazy val ressunRahoitusmuotoEiTiedossa: Seq[LukioKurssinRahoitusmuotoRow] = raportti.collectFirst {
    case d: DataSheet if d.title == LukioRahoitusmuotoEiTiedossa.sheetTitle => d.rows.collect {
      case r: LukioKurssinRahoitusmuotoRow => r
    }
  }.get

  lazy val ressunOpiskeluoikeudenUlkopuolisetArvionnit: Seq[LukioOppiaineOpiskeluoikeudenUlkopuolisetRow] = raportti.collectFirst {
    case d: DataSheet if d.title == LukioOppiaineOpiskeluoikeudenUlkopuoliset.sheetTitle => d.rows.collect {
      case r: LukioOppiaineOpiskeluoikeudenUlkopuolisetRow => r
    }
  }.get

  private def loadRaportti = {
    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = MockOrganisaatiot.helsinginKaupunki,
      downloadToken = None,
      password = "foobar",
      alku = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku,
      loppu = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoLoppu
    )

    new RaportitService(KoskiApplicationForTests).lukioKoulutuksenKurssikertyma(request).sheets
  }
}
