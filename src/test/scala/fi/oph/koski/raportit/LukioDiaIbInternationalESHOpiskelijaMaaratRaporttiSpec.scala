package fi.oph.koski.raportit


import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.lukio.LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiSpec extends AnyFreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll {
  //Note: Raportin latauspäivä ja raportin fixtuurit käyttävät dynaamista päivämäärää, kts. LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.scala

  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  override def defaultUser = MockUsers.helsinginKaupunkiPalvelukäyttäjä

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  val hese = "Helsingin medialukio"
  val resu = "Ressun lukio"

  "Lukion opiskelijamäärät raportti" - {
    "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
      AuditLogTester.clearMessages
      authGet(s"api/raportit/lukiodiaibinternationaleshopiskelijamaarat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukiokoulutus_opiskelijamaarat_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiodiaibinternationaleshopiskelijamaarat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&lang=fi")))
      }
    }

    "Raportin lataaminen onnistuu eri lokalisaatiolla ja tuottaa auditlogin" in {
      AuditLogTester.clearMessages
      authGet(s"api/raportit/lukiodiaibinternationaleshopiskelijamaarat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="gymnasieutbildning_antal_studerande_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiodiaibinternationaleshopiskelijamaarat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&lang=sv")))
      }
    }

    lazy val rivit: Seq[LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow] = loadRaportti
    lazy val helsinki = rivit.find(_.oppilaitosNimi == hese).get
    lazy val ressu = rivit.find(_.oppilaitosNimi == resu).get

    "Tiedot jaotellaan oppilaitoksittain" in {
      rivit.map(_.oppilaitosNimi) should contain theSameElementsAs (Seq(hese, resu))
    }
    "Opiskelijoiden määrä" in {
      helsinki.opiskelijoidenMaara shouldBe(2)
      helsinki.oppimaaranSuorittajia shouldBe(2)
      helsinki.nuortenOppimaaranSuorittajia shouldBe(1)
      helsinki.aikuistenOppimaaranSuorittajia shouldBe(1)
      helsinki.aineopiskelija shouldBe(0)

      ressu.opiskelijoidenMaara shouldBe(5)
      ressu.oppimaaranSuorittajia shouldBe(4)
      ressu.nuortenOppimaaranSuorittajia shouldBe(4)
      ressu.aikuistenOppimaaranSuorittajia shouldBe(0)
      ressu.aineopiskelija shouldBe(1)
    }
    "Valtionosuus rahoitteisia" in {
      helsinki.opiskelijoidenMaara_VOSRahoitteisia should equal(1)
      helsinki.oppimaaranSuorittajia_VOSRahoitteisia shouldBe(1)
      helsinki.nuortenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(1)
      helsinki.aikuistenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(0)
      helsinki.aineopiskelija_VOSRahoitteisia shouldBe(0)

      ressu.opiskelijoidenMaara_VOSRahoitteisia should equal(2)
      ressu.oppimaaranSuorittajia_VOSRahoitteisia shouldBe(1)
      ressu.nuortenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(1)
      ressu.aikuistenOppimaaranSuorittajia_VOSRahoitteisia shouldBe(0)
      ressu.aineopiskelija_VOSRahoitteisia shouldBe(1)
    }
    "Muuta kautta rahoitettu" in {
      helsinki.opiskelijoidenMaara_MuutaKauttaRahoitettu should equal(1)
      helsinki.oppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(1)
      helsinki.nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(0)
      helsinki.aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(1)
      helsinki.aineopiskelija_MuutaKauttaRahoitettu shouldBe(0)

      ressu.opiskelijoidenMaara_MuutaKauttaRahoitettu should equal(3)
      ressu.oppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(3)
      ressu.nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(3)
      ressu.aikuistenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(0)
      ressu.aineopiskelija_MuutaKauttaRahoitettu shouldBe(0)
    }
    "Ulkomaisia vaihto-opiskelijoita" in {
      helsinki.opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita shouldBe(1)
      helsinki.oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita shouldBe(1)
      helsinki.aineopiskeija_UlkomaisiaVaihtoOpiskelijoita shouldBe(0)

      ressu.opiskelijoidenMaara_UlkomaisiaVaihtoOpiskelijoita shouldBe(0)
      ressu.oppimaaranSuorittajia_UlkomaisiaVaihtoOpiskelijoita shouldBe(0)
      ressu.aineopiskeija_UlkomaisiaVaihtoOpiskelijoita shouldBe(0)
    }
    "Neljännen vuoden opiskelijoita" in {
      helsinki.oppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(1)
      helsinki.nuortenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(1)
      helsinki.aikuistenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(0)

      ressu.oppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(0)
      ressu.nuortenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(0)
      ressu.aikuistenOppimaaranSuorittajia_YliKolmeVuotta_VOSRahoitteisia shouldBe(0)
    }
    "Opetuskieli suomi" in {
      helsinki.oppimaaranSuorittajia_OpetuskieliSuomi shouldBe(1)
      helsinki.nuortenOppimaaranSuorittajia_OpetuskieliSuomi shouldBe(1)
      helsinki.aikuistenOppimaaranSuorittajia_OpetuskieliSuomi shouldBe(0)

      ressu.oppimaaranSuorittajia_OpetuskieliSuomi shouldBe(0)
      ressu.nuortenOppimaaranSuorittajia_OpetuskieliSuomi shouldBe(0)
      ressu.aikuistenOppimaaranSuorittajia_OpetuskieliSuomi shouldBe(0)
    }
    "Opetuskieli ruotsi" in {
      helsinki.oppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(1)
      helsinki.nuortenOppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(0)
      helsinki.aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(1)

      ressu.oppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(0)
      ressu.nuortenOppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(0)
      ressu.aikuistenOppimaaranSuorittajia_OpetuskieliRuotsi shouldBe(0)
    }
    "Opetuskieli muu" in {
      helsinki.oppimaaranSuorittajia_OpetuskieliMuu shouldBe(0)
      helsinki.nuortenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(0)
      helsinki.aikuistenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(0)

      ressu.oppimaaranSuorittajia_OpetuskieliMuu shouldBe(4)
      ressu.nuortenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(4)
      ressu.aikuistenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(0)
    }
    "Sisäoppilaitosmainen majoitus" in {
      helsinki.oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus_VOSRahoitteisia shouldBe(1)
      ressu.oppimaaranSuorittajia_SisaoppilaitosmainenMajoitus_VOSRahoitteisia shouldBe(0)
    }
    "Erityinen koulutustehtävä" in {
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_101 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_102 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_103 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_104 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_105 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_106 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_107 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_108 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_109 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_208 should equal(0)
      helsinki.oppimaaranSuorittajia_ErityinenKoulutustehtava_211 should equal(0)

      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_101 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_102 should equal(1)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_103 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_104 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_105 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_106 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_107 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_108 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_109 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_208 should equal(0)
      ressu.oppimaaranSuorittajia_ErityinenKoulutustehtava_211 should equal(0)
    }
  }

  private def loadRaportti = {
    val request = RaporttiPäivältäRequest(
      oppilaitosOid = MockOrganisaatiot.helsinginKaupunki,
      downloadToken = None,
      password = "foobar",
      paiva = LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiFixtures.date,
      lang = "fi"
    )
    new RaportitService(KoskiApplicationForTests)
      .lukioDiaIbInternationalESHOpiskelijaMaaratRaportti(request, t)
      .sheets.head.asInstanceOf[DataSheet]
      .rows.asInstanceOf[Seq[LukioDiaIbInternationalESHOpiskelijaMaaratRaporttiRow]]
  }
}
