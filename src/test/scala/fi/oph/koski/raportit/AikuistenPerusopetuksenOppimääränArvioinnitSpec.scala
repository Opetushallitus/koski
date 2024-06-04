package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus._
import fi.oph.koski.documentation.{ExamplesAikuistenPerusopetus, PerusopetusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.tyhjä
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenOppimääräArvioinnit, AikuistenPerusopetuksenOppimääränArvioinnitRow, AikuistenPerusopetuksenOppimääränKurssikertymät, AikuistenPerusopetuksenOppimääränKurssikertymätRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.{List, Seq}

class AikuistenPerusopetuksenOppimääränArvioinnitSpec
  extends AnyFreeSpec
    with Matchers
    with DirtiesFixtures
    with RaportointikantaTestMethods
    with BeforeAndAfterAll
    with PutOpiskeluoikeusTestMethods[AikuistenPerusopetuksenOpiskeluoikeus] {

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = AikuistenPerusopetuksenOppimääräArvioinnit(application.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private val aikaisintaan: LocalDate = date(2006, 1, 1)
  private val viimeistaan: LocalDate = date(2018, 12, 30)
  private lazy val raportti = raporttiBuilder.build(List(jyväskylänNormaalikoulu), aikaisintaan, viimeistaan, t)(session(defaultUser))
  private lazy val raporttiRows = raportti.rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppimääränArvioinnitRow])

  override protected def alterFixture(): Unit = {
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
      aikuistenPerusopetuksenAlkuvaiheenSuoritus,
      aikuistenPerusopetukseOppimääränSuoritus(aikuistenPerusopetus2017, oppiaineidenSuoritukset2017)
    ),
    lisätiedot = Some(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(vaikeastiVammainen = Some(List(Aikajakso(date(2014, 6, 6), None)))))
  )

  "Aikuisten perusopetuksen oppimääräopiskelijoiden arviointien raportti" - {
    "Raportin kolumnit" in {
      raportti.columnSettings.map(_._1) should equal(List(
        "opiskeluoikeusOid",
        "alkamispaiva",
        "paattymispaiva",
        "viimeisinTila",
        "suorituksenTyyppi",
        "kurssinKoodi",
        "kurssinNimi",
        "paikallinenModuuli",
        "arviointiPvm",
        "arviointiKoodiarvo",
        "ensimmainenArviointiPvm",
        "hylatynKorotus",
        "hyvaksytynKorotus",
        "arviointienlkm"
      ))
    }
  }

  private def session(user: KoskiMockUser)= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
