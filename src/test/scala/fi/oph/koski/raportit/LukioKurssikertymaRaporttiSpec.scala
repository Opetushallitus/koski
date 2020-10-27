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
    loadRaportointikantaFixtures
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
      "Suoritettuja" in {
        helsinkiOppimaara.suoritettujaKursseja shouldBe(3)
      }
      "Tunnustettuja" in {
        helsinkiOppimaara.tunnustettujaKursseja shouldBe(4)
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
        ressunAineopiskelijat.kurssejaYhteensa shouldBe(10)
      }
      "Suoritettuja" in {
        ressunAineopiskelijat.suoritettujaKursseja shouldBe(4)
      }
      "Tunnustettuja" in {
        ressunAineopiskelijat.tunnustettujaKursseja shouldBe(6)
      }
      "Tunnustettuja rahoituksen piirissä" in {
        ressunAineopiskelijat.tunnustettujaKursseja_rahoituksenPiirissa shouldBe(3)
      }
      "Pakollisia tai valtakunnallinen ja syventava" in {
        ressunAineopiskelijat.pakollisia_tai_valtakunnallisiaSyventavia shouldBe(7)
      }
      "Pakollisia" in {
        ressunAineopiskelijat.pakollisiaKursseja shouldBe(4)
      }
      "Valtakunnallisia syventavia" in {
        ressunAineopiskelijat.valtakunnallisestiSyventaviaKursseja shouldBe(3)
      }
      "Suoritettuja pakollisia ja suoritettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.suoritettujaPakollisia_ja_suoritettujaValtakunnallisiaSyventavia shouldBe(3)
      }
      "Suoritettuja pakollisia" in {
        ressunAineopiskelijat.suoritettujaPakollisiaKursseja shouldBe(2)
      }
      "Suoritettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.suoritettujaValtakunnallisiaSyventaviaKursseja shouldBe(1)
      }
      "Tunnustettuja pakollisia" in {
        ressunAineopiskelijat.tunnustettujaPakollisiaKursseja shouldBe(2)
      }
      "Tunnustettuja valtakunnallisia syventavia" in {
        ressunAineopiskelijat.tunnustettujaValtakunnallisiaSyventaviaKursseja shouldBe(2)
      }
      "Tunnustettuja rahoituksen piirissa pakollisia" in {
        ressunAineopiskelijat.tunnustettuja_rahoituksenPiirissa_pakollisia shouldBe(1)
      }
      "Tunnustettuja rahoituksen piirissa valtakunnallisesti syventavia" in {
        ressunAineopiskelijat.tunnustettuja_rahoituksenPiirissa_valtakunnallisiaSyventaiva shouldBe(1)
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
