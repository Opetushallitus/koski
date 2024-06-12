package fi.oph.koski.raportit

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus._
import fi.oph.koski.documentation.{ExamplesAikuistenPerusopetus, PerusopetusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.tyhjä
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenOppimääräArvioinnit, AikuistenPerusopetuksenOppimääränArvioinnitRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

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
    val oo  = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen.copy(
      tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(
        List(
          AikuistenPerusopetuksenOpiskeluoikeusjakso(aikaisintaan.minusYears(1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        )
      ),
      suoritukset = List(
        aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
          osasuoritukset = Some(List(
            alkuvaiheenOppiaineenSuoritus(AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = "AI1", koodistoUri = "oppiaineaidinkielijakirjallisuus"))).copy(
              arviointi = PerusopetusExampleData.arviointi(9, Some(viimeistaan.plusMonths(2))),
              osasuoritukset = Some(List(
                alkuvaiheenKurssinSuoritus("AÄI1").copy(arviointi = Some(List(
                  PerusopetuksenOppiaineenArviointi(4, Some(aikaisintaan.minusMonths(1))),
                  PerusopetuksenOppiaineenArviointi(6, Some(viimeistaan.minusMonths(2))),
                  PerusopetuksenOppiaineenArviointi(5, Some(viimeistaan.minusMonths(1))),
                  PerusopetuksenOppiaineenArviointi(9, Some(viimeistaan.plusMonths(1))),
                ))),
              ))
            )
          ))
        )
      )
    )

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
        "arviointienlkm",
        "ensimmainenArviointiPvm",
        "parasArviointiPvm",
        "parasArviointiArvosana",
        "arviointiPvm",
        "arviointiArvosana",
        "hylatynKorotus",
        "hyvaksytynKorotus",
      ))
    }

    "Raportin rivit" in {
      val rows = raportti.rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppimääränArvioinnitRow])
      val hylatynKorotusRowOpt = rows.find(_.hylatynKorotus.contains(true))

      hylatynKorotusRowOpt should be (defined)
      hylatynKorotusRowOpt.get.arviointienlkm should be(4)

      val opiskeluoikeusOid = hylatynKorotusRowOpt.get.opiskeluoikeusOid
      val arviointiRivit = rows.filter(_.opiskeluoikeusOid == opiskeluoikeusOid)

      arviointiRivit should have size 2

      val firstRow = arviointiRivit.head
      firstRow.hylatynKorotus should be (Some(true))
      firstRow.hyvaksytynKorotus should be (Some(false))
      firstRow.arviointiArvosana should be ("6")

      val lastRow = arviointiRivit.last
      lastRow.hylatynKorotus should be (Some(false))
      lastRow.hyvaksytynKorotus should be (Some(true))
      lastRow.arviointiArvosana should be ("5")
    }
  }

  private def session(user: KoskiMockUser)= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
