package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetukseOppimääränSuoritus, aikuistenPerusopetuksenAlkuvaiheenSuoritus, aikuistenPerusopetus2017, oppiaineidenSuoritukset2017}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.oppiaineenKorottaja
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät, AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DirtiesFixtures
    with PutOpiskeluoikeusTestMethods[AikuistenPerusopetuksenOpiskeluoikeus] {

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät(application.raportointiDatabase.db)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), date(2006, 1, 1), date(2016, 12, 30), t)(session(defaultUser)).rows.map(_.asInstanceOf[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow])

  override protected def alterFixture(): Unit = {
    lisääPäätasonSuorituksia(
      oppiaineenKorottaja,
      List(
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusAI1,
        ExamplesAikuistenPerusopetus.oppiaineenOppimääränSuoritusYH
      )
    )
    reloadRaportointikanta()
  }

  def tag = implicitly[reflect.runtime.universe.TypeTag[AikuistenPerusopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo, oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)) = AikuistenPerusopetuksenOpiskeluoikeus(
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
      List(
        AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, Some(valtionosuusRahoitteinen))
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
    "Raportin kolumnit" in {
      lazy val r = findSingle(raportti)

      r.oppilaitos should equal("Jyväskylän normaalikoulu")
      r.yhteensäSuorituksia should equal(2)
      r.yhteensäSuoritettujaSuorituksia should equal(2)
      r.yhteensäTunnistettujaSuorituksia should equal(0)
      r.yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä should equal(0)
      r.päättövaiheenSuorituksia should equal(2)
      r.päättövaiheenSuoritettujaSuorituksia should equal(2)
      r.päättövaiheenTunnistettujaSuorituksia should equal(0)
      r.päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä should equal(0)
      r.alkuvaiheenSuorituksia should equal(0)
      r.alkuvaiheenSuoritettujaSuorituksia should equal(0)
      r.alkuvaiheenTunnistettujaSuorituksia should equal(0)
      r.alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä should equal(0)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut should equal(0)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa should equal(0)
      r.suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa should equal(0)
    }
  }

  private def findSingle(rows: Seq[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow]) = {
    val found = rows.filter(_.oppilaitos.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private def lisääPäätasonSuorituksia(oppija: LaajatOppijaHenkilöTiedot, päätasonSuoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus]) = {
    val oo = getOpiskeluoikeus(oppija.oid, OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo).asInstanceOf[AikuistenPerusopetuksenOpiskeluoikeus]
    putOppija(Oppija(oppija, List(oo.copy(suoritukset = päätasonSuoritukset ::: oo.suoritukset)))) {
      verifyResponseStatusOk()
    }
  }
}
