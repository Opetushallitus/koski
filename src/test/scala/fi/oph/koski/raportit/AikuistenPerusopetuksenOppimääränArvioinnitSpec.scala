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

  var resultOo = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

  override protected def alterFixture(): Unit = {
    resultOo = setupOppijaWithAndGetOpiskeluoikeus(
      ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen.copy(
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
                    PerusopetuksenOppiaineenArviointi(9, Some(viimeistaan.plusMonths(1)))
                  ))),
                  alkuvaiheenKurssinSuoritus("AÄI2").copy(arviointi = Some(List(
                    PerusopetuksenOppiaineenArviointi(4, Some(aikaisintaan.plusWeeks(1))),   // ei hyväksytyn eikä hylätyn korotus
                    PerusopetuksenOppiaineenArviointi("H", None, Some(aikaisintaan.plusWeeks(2))), // hylätyn korotus
                    PerusopetuksenOppiaineenArviointi(4, Some(aikaisintaan.plusWeeks(3))),   // hylätyn korotus
                  ))),
                  alkuvaiheenKurssinSuoritus("AÄI3").copy(arviointi = Some(List(
                    PerusopetuksenOppiaineenArviointi(5, Some(aikaisintaan.plusWeeks(1))), // ei hyväksytyn eikä hylätyn korotus
                    PerusopetuksenOppiaineenArviointi(4, Some(aikaisintaan.plusWeeks(2))), // hyväksytyn korotus
                    PerusopetuksenOppiaineenArviointi("H", None, Some(aikaisintaan.plusWeeks(3))), // hylätyn korotus
                    PerusopetuksenOppiaineenArviointi(5, Some(aikaisintaan.plusWeeks(4))), // hylätyn korotus
                    PerusopetuksenOppiaineenArviointi(5, Some(aikaisintaan.plusWeeks(5))), // hyväksytyn korotus
                  )))
                ))
              )
            ))
          )
        )
      ),
      tyhjä.copy(hetu = "010106A8691")
    )

    reloadRaportointikanta
  }

  override def defaultOpiskeluoikeus = ExamplesAikuistenPerusopetus.aikuistenPerusopetuksenOpiskeluoikeusAlkuvaiheineen

  def tag = implicitly[reflect.runtime.universe.TypeTag[AikuistenPerusopetuksenOpiskeluoikeus]]

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

      val arviointiRivit = rows
        .filter(_.opiskeluoikeusOid == resultOo.oid.get)
        .filter(_.kurssinKoodi == "AÄI1")

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

    "Raportin rivit ÄI2" in {

      val rows = raportti.rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppimääränArvioinnitRow])
      val arviointiRivit = rows
        .filter(_.opiskeluoikeusOid == resultOo.oid.get)
        .filter(_.kurssinKoodi == "AÄI2")

      arviointiRivit should have size 3

      val firstRow = arviointiRivit.head
      firstRow.hylatynKorotus should be (Some(false))
      firstRow.hyvaksytynKorotus should be (Some(false))

      val secondRow = arviointiRivit.apply(1)
      secondRow.hylatynKorotus should be (Some(true))
      secondRow.hyvaksytynKorotus should be (Some(false))

      val lastRow = arviointiRivit.last
      lastRow.hylatynKorotus should be (Some(true))
      lastRow.hyvaksytynKorotus should be (Some(false))
    }

    "Raportin rivit ÄI3" in {
      val rows = raportti.rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppimääränArvioinnitRow])
      val arviointiRivit = rows
        .filter(_.opiskeluoikeusOid == resultOo.oid.get)
        .filter(_.kurssinKoodi == "AÄI3")

      arviointiRivit should have size 5

      val firstRow = arviointiRivit.head
      firstRow.hylatynKorotus should be (Some(false))
      firstRow.hyvaksytynKorotus should be (Some(false))

      val secondRow = arviointiRivit.apply(1)
      secondRow.hylatynKorotus should be (Some(false))
      secondRow.hyvaksytynKorotus should be (Some(true))

      val thirdRow = arviointiRivit.apply(2)
      thirdRow.hylatynKorotus should be (Some(true))
      thirdRow.hyvaksytynKorotus should be (Some(false))

      val fourthRow = arviointiRivit.apply(3)
      fourthRow.hylatynKorotus should be (Some(true))
      fourthRow.hyvaksytynKorotus should be (Some(false))

      val lastRow = arviointiRivit.last
      lastRow.hylatynKorotus should be (Some(false))
      lastRow.hyvaksytynKorotus should be (Some(true))
    }
  }

  private def session(user: KoskiMockUser)= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
