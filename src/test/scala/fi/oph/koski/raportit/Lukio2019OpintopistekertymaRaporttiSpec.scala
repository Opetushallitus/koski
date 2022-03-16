package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioKurssikertymaRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio._
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class Lukio2019OpintopistekertymaRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll {

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  override def defaultUser = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä

  "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
    AuditLogTester.clearMessages
    authGet(s"api/raportit/lukio2019opintopistekertymat?oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2016-01-01&loppu=2022-01-01&lang=fi&password=salasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukio2019_opintopistekertymat_20160101-20220101.xlsx"""")
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukio2019opintopistekertymat&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&alku=2016-01-01&loppu=2022-01-01&lang=fi")))
    }
  }
  /*
  "Excel välilehtien sarakkeet, valitaan vain ne kurssit joiden arviointipäivä on aikavälin sisällä" - {
    "Oppimäärän välilehti (lukio ops 2015)" - {
      "OppilaitosOid" in {
        jyväskylänOppimäärä.oppilaitosOid shouldBe(MockOrganisaatiot.helsinginMedialukio)
      }
      "Suoritettuja" in {
        jyväskylänOppimäärä.suoritettujaKursseja shouldBe(2)
      }
      "Tunnustettuja" in {
        jyväskylänOppimäärä.tunnustettujaKursseja shouldBe(4)
      }
      "Kursseja yhteensä" in {
        jyväskylänOppimäärä.kurssejaYhteensa shouldBe(6)
      }
      "Tunnustettuja rahoituksen piirissa" in {
        jyväskylänOppimäärä.tunnustettujaKursseja_rahoituksenPiirissa shouldBe(2)
      }
    }
  }*/

  lazy val raportti = loadRaportti

  lazy val jyväskylänOppimäärä = raportti.collectFirst {
    case d: DataSheet if d.title == t.get("raportti-excel-oppimäärä-sheet-name") => d.rows.collect {
      case r: LukioKurssikertymaOppimaaraRow => r
    }
  }.get.find(_.oppilaitos == "Jyväskylän normaalikoulu").get

  private def loadRaportti = {
    val request = AikajaksoRaporttiRequest(
      oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu,
      downloadToken = None,
      password = "foobar",
      alku = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoAlku,
      loppu = LukioKurssikertymaRaporttiFixtures.raportinAikajaksoLoppu,
      lang = "fi"
    )

    new RaportitService(KoskiApplicationForTests).lukioKoulutuksenKurssikertyma(request, t).sheets
  }
}
