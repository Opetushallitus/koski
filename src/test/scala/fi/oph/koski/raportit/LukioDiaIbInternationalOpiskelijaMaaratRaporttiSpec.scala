package fi.oph.koski.raportit


import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.fixture.LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class LukioDiaIbInternationalOpiskelijaMaaratRaporttiSpec extends FreeSpec with RaportointikantaTestMethods with BeforeAndAfterAll {
  //Note: Raportin latauspäivä ja raportin fixtuurit käyttävät dynaamista päivämäärää, kts. LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.scala

  override def defaultUser = MockUsers.helsinginKaupunkiPalvelukäyttäjä

  override def beforeAll() = {
    loadRaportointikantaFixtures
  }

  val hese = "Helsingin medialukio"
  val resu = "Ressun lukio"

  "Lukion opiskelijamäärät raportti" - {
    "Raportin lataaminen onnistuu ja tuottaa auditlogin" in {
      AuditLogTester.clearMessages
      authGet(s"api/raportit/lukiodiaibinternationalopiskelijamaarat?oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lukiokoulutus_opiskelijamaarat_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=lukiodiaibinternationalopiskelijamaarat&oppilaitosOid=${MockOrganisaatiot.helsinginKaupunki}&paiva=2018-01-01")))
      }
    }

    lazy val rivit: Seq[LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow] = loadRaportti
    lazy val helsinki = rivit.find(_.oppilaitos == hese).get
    lazy val ressu = rivit.find(_.oppilaitos == resu).get

    "Tiedot jaotellaan oppilaitoksittain" in {
      rivit.map(_.oppilaitos) should contain theSameElementsAs (Seq(hese, resu))
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

      ressu.opiskelijoidenMaara_MuutaKauttaRahoitettu should equal(1)
      ressu.oppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(1)
      ressu.nuortenOppimaaranSuorittajia_MuutaKauttaRahoitettu shouldBe(1)
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
      helsinki.oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(2)
      helsinki.nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(1)
      helsinki.aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(1)

      ressu.oppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(0)
      ressu.nuortenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(0)
      ressu.aikuistenOppimaaranSuorittajia_NeljannenVuodenOpiskelijoita shouldBe(0)
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

      ressu.oppimaaranSuorittajia_OpetuskieliMuu shouldBe(3)
      ressu.nuortenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(3)
      ressu.aikuistenOppimaaranSuorittajia_OpetuskieliMuu shouldBe(0)
    }
    "Sisäoppilaitosmainen majoitus" in {
      helsinki.opiskelijoidenMaara_SisaoppilaitosmainenMajoitus shouldBe(1)
      ressu.opiskelijoidenMaara_SisaoppilaitosmainenMajoitus shouldBe(0)
    }
    "Erityinen koulutustehtävä" in {
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_101 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_102 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_103 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_104 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_105 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_106 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_107 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_108 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_109 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_208 should equal(0)
      helsinki.opiskelijoidenMaara_ErityinenKoulutustehtava_211 should equal(0)

      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_101 should equal(1)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_102 should equal(1)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_103 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_104 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_105 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_106 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_107 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_108 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_109 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_208 should equal(0)
      ressu.opiskelijoidenMaara_ErityinenKoulutustehtava_211 should equal(0)
    }
  }

  private def loadRaportti = {
    val request = RaporttiPäivältäRequest(
      oppilaitosOid = MockOrganisaatiot.helsinginKaupunki,
      downloadToken = None,
      password = "foobar",
      paiva = LukioDiaIbInternationalOpiskelijaMaaratRaporttiFixtures.date
    )
    new RaportitService(KoskiApplicationForTests)
      .lukioDiaIbInternationalOpiskelijaMaaratRaportti(request)
      .sheets.head.asInstanceOf[DataSheet]
      .rows.asInstanceOf[Seq[LukioDiaIbInternationalOpiskelijaMaaratRaporttiRow]]
  }
}
