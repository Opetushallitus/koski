package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.{ExamplesAikuistenPerusopetus, PerusopetusExampleData}
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetukseOppimääränSuoritus, aikuistenPerusopetuksenAlkuvaiheenSuoritus, aikuistenPerusopetus2017, alkuvaiheenKurssinSuoritus, alkuvaiheenOppiaineenSuoritus, oppiaineidenSuoritukset2017}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.tyhjä
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenOppimääränKurssikertymät, AikuistenPerusopetuksenOppimääränKurssikertymätRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{Aikajakso, AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus, AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot, AikuistenPerusopetuksenOpiskeluoikeudenTila, AikuistenPerusopetuksenOpiskeluoikeus, AikuistenPerusopetuksenOpiskeluoikeusjakso, Koodistokoodiviite, Oppilaitos, PerusopetuksenOppiaineenArviointi, SisältäväOpiskeluoikeus}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class AikuistenPerusopetuksenOppimääränKurssikertymätSpec
  extends AnyFreeSpec
    with Matchers
    with DirtiesFixtures
    with RaportointikantaTestMethods
    with BeforeAndAfterAll
    with PutOpiskeluoikeusTestMethods[AikuistenPerusopetuksenOpiskeluoikeus] {

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = AikuistenPerusopetuksenOppimääränKurssikertymät(application.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private val aikaisintaan: LocalDate = date(2006, 1, 1)
  private val viimeistaan: LocalDate = date(2018, 12, 30)
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), aikaisintaan, viimeistaan, t)(session(defaultUser)).rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppimääränKurssikertymätRow])

  override protected def alterFixture(): Unit = {
    val ooEronnut = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineenValmistunutVanhanOppivelvollisuuslainAikana.copy(
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
          AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 6, 4), opiskeluoikeusEronnut, Some(valtionosuusRahoitteinen))
        )
      ),
      suoritukset = List(
        aikuistenPerusopetuksenAlkuvaiheenSuoritus().copy(
          osasuoritukset = Some(List(
            alkuvaiheenOppiaineenSuoritus(AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = "AI1", koodistoUri = "oppiaineaidinkielijakirjallisuus"))).copy(
              arviointi = PerusopetusExampleData.arviointi(9, Some(date(2016, 12, 29))),
              osasuoritukset = Some(List(
                alkuvaiheenKurssinSuoritus("AÄI1").copy(arviointi = PerusopetusExampleData.arviointi(9, Some(date(2018, 12, 29)))),
              ))
            )
          ))
        )
      )
    )

    val oo  = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineenValmistunutVanhanOppivelvollisuuslainAikana.copy(
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(aikaisintaan.minusYears(1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        )
      ),
      suoritukset = List(
        aikuistenPerusopetuksenAlkuvaiheenSuoritus().copy(
          osasuoritukset = Some(List(
            alkuvaiheenOppiaineenSuoritus(AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = "AI1", koodistoUri = "oppiaineaidinkielijakirjallisuus"))).copy(
              arviointi = PerusopetusExampleData.arviointi(9, Some(viimeistaan.plusMonths(2))),
              osasuoritukset = Some(List(
                alkuvaiheenKurssinSuoritus("AÄI1").copy(arviointi = Some(List(
                  PerusopetuksenOppiaineenArviointi(7, Some(aikaisintaan)),
                  PerusopetuksenOppiaineenArviointi(8, Some(viimeistaan.minusMonths(1))),
                  PerusopetuksenOppiaineenArviointi(9, Some(viimeistaan.plusMonths(1))),
                ))),
              ))
            )
          ))
        )
      )
    )

    putOpiskeluoikeus(ooEronnut, tyhjä) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(oo, tyhjä.copy(hetu = "010106A8691")) {
      verifyResponseStatusOk()
    }

    reloadRaportointikanta
  }

  def tag = implicitly[reflect.runtime.universe.TypeTag[AikuistenPerusopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo, oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)) = AikuistenPerusopetuksenOpiskeluoikeus(
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
      List(
        AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 6, 4), opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
      )
    ),
    koulutustoimija = None,
    oppilaitos = Some(oppilaitos),
    suoritukset = List(
      aikuistenPerusopetuksenAlkuvaiheenSuoritus(),
      aikuistenPerusopetukseOppimääränSuoritus(aikuistenPerusopetus2017, oppiaineidenSuoritukset2017)
    ),
    lisätiedot = Some(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(vaikeastiVammainen = Some(List(Aikajakso(date(2014, 6, 6), None)))))
  )

  "Aikuisten perusopetuksen oppimääräopiskelijoiden kurssikertymien raportti" - {
      "Syötetään pihvi ja kuori" in {
        val jyväskylänOpiskeluoikeus: AikuistenPerusopetuksenOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, defaultHenkilö, user = paakayttaja)
        val kuoriOpiskeluoikeus = createLinkitetytOpiskeluoikeudet(jyväskylänOpiskeluoikeus, MockOrganisaatiot.jyväskylänNormaalikoulu).copy(
          suoritukset = List(
            aikuistenPerusopetuksenAlkuvaiheenSuoritus(),
            aikuistenPerusopetukseOppimääränSuoritus(aikuistenPerusopetus2017, oppiaineidenSuoritukset2017)
          ),
        )

        putOpiskeluoikeus(kuoriOpiskeluoikeus)(verifyResponseStatusOk())
      }

    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/aikuistenperusopetuksenkurssikertymaraportti?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="aikuisten_perusopetuksen_kurssikertymät_raportti-20060101-20161230.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/aikuistenperusopetuksenkurssikertymaraportti?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="grundläggande_vuxna_kursantal_rapport-20060101-20161230.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=sv")))
      }
    }

    "Raportin kolumnit" in {
      lazy val r = findSingle(raportti)

      r.oppilaitos should equal("Jyväskylän normaalikoulu")
      r.yhteensäSuorituksia should equal(33)
      r.yhteensäSuoritettujenArviointeja should equal(38)
      r.yhteensäSuoritettujaSuorituksia should equal(27)
      r.yhteensäTunnistettujaSuorituksia should equal(6)
      r.yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä should equal(3)
      r.päättövaiheenSuorituksia should equal(6)
      r.päättövaiheenSuoritettujaSuorituksia should equal(2)
      r.päättövaiheenTunnistettujaSuorituksia should equal(4)
      r.päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä should equal(2)
      r.alkuvaiheenSuorituksia should equal(27)
      r.alkuvaiheenSuoritettujaSuorituksia should equal(25)
      r.alkuvaiheenTunnistettujaSuorituksia should equal(2)
      r.alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä should equal(1)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut should equal(2)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa should equal(0)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa should equal(2)
      r.eriVuonnaKorotetutSuoritukset should equal(3)
    }
  }

  private def findSingle(rows: Seq[AikuistenPerusopetuksenOppimääränKurssikertymätRow]) = {
    val found = rows.filter(_.oppilaitos.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def session(user: KoskiMockUser)= user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private def createLinkitetytOpiskeluoikeudet(kuoriOpiskeluoikeus: AikuistenPerusopetuksenOpiskeluoikeus, pihviOppilaitos: Oid) = {
    val pihviOpiskeluoikeus = makeOpiskeluoikeus(oppilaitos = Oppilaitos(pihviOppilaitos)).copy(
      oid = kuoriOpiskeluoikeus.oid,
      suoritukset = List(
        aikuistenPerusopetuksenAlkuvaiheenSuoritus(),
        aikuistenPerusopetukseOppimääränSuoritus(aikuistenPerusopetus2017, oppiaineidenSuoritukset2017)
      ),
      sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(kuoriOpiskeluoikeus.oppilaitos.get, kuoriOpiskeluoikeus.oid.get))
    )
    createOpiskeluoikeus(defaultHenkilö, pihviOpiskeluoikeus, user = paakayttaja)
    kuoriOpiskeluoikeus.copy(
      versionumero = None,
      oppilaitos = None,
      koulutustoimija = None
    )
  }
}
