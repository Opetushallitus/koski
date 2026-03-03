package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusEronnut, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetukseOppimääränSuoritus, aikuistenPerusopetuksenAlkuvaiheenSuoritus, aikuistenPerusopetus2017, oppiaineidenSuoritukset2017}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{aikuisOpiskelija, tyhjä}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus._
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssitSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DirtiesFixtures
    with PutOpiskeluoikeusTestMethods[AikuistenPerusopetuksenOpiskeluoikeus] {

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssit(application.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), date(2006, 1, 1), date(2018, 12, 30), t)(session(defaultUser)).rows.map(_.asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeudenUlkopuolisetKurssitRow])

  override protected def alterFixture(): Unit = {
    val ooEronnut = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineenValmistunutVanhanOppivelvollisuuslainAikana.copy(
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
          AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 6, 4), opiskeluoikeusEronnut, Some(valtionosuusRahoitteinen))
        )
      ),
      suoritukset = List(
        aikuistenPerusopetuksenAlkuvaiheenSuoritus()
      )
    )
    putOpiskeluoikeus(ooEronnut, tyhjä){
      verifyResponseStatusOk()
    }

    reloadRaportointikanta()
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

  "Aikuisten perusopetuksen aineopiskelijoiden kurssikertymien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/aikuistenperusopetuksenkurssikertymaraportti?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="aikuisten_perusopetuksen_kurssikertymät_raportti-20060101-20161230.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/aikuistenperusopetuksenkurssikertymaraportti?oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="grundläggande_vuxna_kursantal_rapport-20060101-20161230.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=aikuistenperusopetuksenkurssikertymaraportti&oppilaitosOid=$jyväskylänNormaalikoulu&alku=2006-01-01&loppu=2016-12-30&lang=sv")))
      }
    }

    "Raportin kolumnit" in {
      lazy val rows = raportti.filter(_.oppilaitos.equals("Jyväskylän normaalikoulu"))
      rows.length should equal (2)

      lazy val row = rows.head
      row.oppilaitos should equal("Jyväskylän normaalikoulu")
      row.kurssikoodi should equal("AÄI1")
      row.kurssinNimi should equal("Suomen kielen ja kirjallisuuden opiskelun perustaidot")
      row.päätasonSuorituksenTyyppi should equal("aikuistenperusopetuksenoppimaaranalkuvaihe")
      row.kurssinSuorituksenTyyppi should equal("aikuistenperusopetuksenalkuvaiheenkurssi")
    }
  }

  private def session(user: KoskiMockUser)= user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private def lisääPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus]) = {
    val oo = getOpiskeluoikeus(oppija.oid, OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo).asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeus]
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = päätasonSuoritukset ::: oo.suoritukset)))) {
      verifyResponseStatusOk()
    }
  }
}

