package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
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
    reloadRaportointikanta()
  }

  "Lukioon valmistavan koulutuksen opiskelijamaarat" - {

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
      paiva = LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.fixedDate,
      lang = "fi"
    )
    new RaportitService(KoskiApplicationForTests)
      .lukioonValmistavanKoulutuksenOpiskelijaMaaratRaportti(request, t)
      .sheets.collectFirst { case d: DataSheet => d.rows.collect {
        case r: LukioonValmistavanKoulutuksenOpiskelijamaaratRaporttiRow => r
    }}.get
  }
}
