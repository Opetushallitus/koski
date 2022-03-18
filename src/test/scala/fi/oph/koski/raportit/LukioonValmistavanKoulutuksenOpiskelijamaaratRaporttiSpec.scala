package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio.LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll {

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override def defaultUser = MockUsers.helsinginKaupunkiPalvelukäyttäjä

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "Lukioon valmistavan koulutuksen opiskelijamaarat" - {

    "Raportin voi ladata ja se tuottaa auditlogin" in {
      AuditLogTester.clearMessages
      authGet(s"api/raportit/luvaopiskelijamaarat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukioon_valmistavan_koulutuksen_opiskelijamaarat_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=luvaopiskelijamaarat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&lang=fi")))
      }
    }

    "Raportin voi ladata eri lokalisaatiolla ja se tuottaa auditlogin" in {
      AuditLogTester.clearMessages
      authGet(s"api/raportit/luvaopiskelijamaarat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukioon_valmistavan_koulutuksen_opiskelijamaarat_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=luvaopiskelijamaarat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&lang=sv")))
      }
    }

    lazy val helsinginRaportti = loadRaportti(MockOrganisaatiot.helsinginKaupunki)
    lazy val ressunRaportti = loadRaportti(MockOrganisaatiot.ressunLukio)
    lazy val ressu = ressunRaportti.find(_.oppilaitos == "Ressun lukio").get

    "Jos haetaan koulutustoimijan oidilla, valitaan raportille sen alaiset organisaatiot" in {
      helsinginRaportti.map(_.oppilaitos) should contain("Ressun lukio")
    }
    "Yhdellä organisaatio oidilla haettaessa raportille otetaan vain sen organisaatio" in {
      ressunRaportti.length shouldBe(1)
    }
    "Opiskelijoiden lukumäärä" in {
      ressu.opiskelijoidenMaara shouldBe(2)
      ressu.nuortenOppimaaranSuorittajia shouldBe(1)
      ressu.aikuistenOppimaaranSuorittajia shouldBe(1)
    }
    "Valtionosuus rahoitteisia" in {
      ressu.opiskelijoidenMaara_VOSRahoitteisia shouldBe(1)
      ressu.nuortenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(1)
      ressu.aikuistenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(0)
    }
    "Muuta kautta rahoitettu" in {
      ressu.opiskelijoidenMaara_MuutaKauttaRahoitettu shouldBe(1)
      ressu.nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(0)
      ressu.aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(1)
    }
    "Kotikunta" in {
      ressu.nuortenOppimaaranSuorittajia_EiKotikuntaa shouldBe(0)
      ressu.nuortenOppimaaranSuorittajia_KotikuntaAhvenanmaa shouldBe(1)

      ressu.aikuistenOppimaaranSuorittajia_EiKotikuntaa shouldBe(1)
      ressu.aikuistenOppimaaranSuorittajia_KotikuntaAhvenanmaa shouldBe(0)
    }
    "Sisäoppilaitosmainen majoitus" in {
      ressu.opiskelijoidenMaara_SisaoppilaitosmainenMajoitus_VOSRahoitteisia shouldBe(1)
    }
  }

  private def loadRaportti(oppilaitosOid: String) = {
    val request = RaporttiPäivältäRequest(
      oppilaitosOid,
      downloadToken = None,
      password = "bassword",
      paiva = LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.fixedDate,
      lang = "fi"
    )
    new RaportitService(KoskiApplicationForTests)
      .lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(request, t)
      .sheets.collectFirst { case d: DataSheet => d.rows.collect {
        case r: LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow => r
    }}.get
  }
}
