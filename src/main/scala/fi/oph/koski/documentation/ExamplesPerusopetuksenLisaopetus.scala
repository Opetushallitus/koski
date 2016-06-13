package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.schema._

object ExamplesPerusopetuksenLisaopetus {
  private def suoritus(aine: PerusopetuksenOppiaine) = PerusopetuksenLisäopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None,
    korotus = false
  )

  val lisäopetuksenPäättötodistus = Oppija(
    exampleHenkilö,
    List(PerusopetuksenLisäopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenLisäopetuksenSuoritus(
          koulutusmoduuli = PerusopetuksenLisäopetus(),
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          osasuoritukset = Some(
            List(
              suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(7), korotus = true),
              suoritus(kieli("A1", "EN")).copy(arviointi = arviointi(10), korotus = true),
              suoritus(kieli("B1", "SV")).copy(arviointi = arviointi(6), korotus = true),
              suoritus(oppiaine("MA")).copy(arviointi = arviointi(6), korotus = true),
              suoritus(oppiaine("BI")).copy(arviointi = arviointi(10), korotus = true),
              suoritus(oppiaine("GE")).copy(arviointi = arviointi(9), korotus = true),
              suoritus(oppiaine("FY")).copy(arviointi = arviointi(8), korotus = true),
              suoritus(oppiaine("KE")).copy(arviointi = arviointi(9), korotus = true),
              suoritus(oppiaine("TE")).copy(arviointi = arviointi(8), korotus = true),
              suoritus(oppiaine("HI")).copy(arviointi = arviointi(7), korotus = false),
              suoritus(oppiaine("YH")).copy(arviointi = arviointi(8), korotus = true),
              suoritus(oppiaine("KU")).copy(arviointi = arviointi(8), korotus = false),
              suoritus(oppiaine("LI")).copy(arviointi = arviointi(7), korotus = true)
            )
          )
        )),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("perusopetuksen lisäopetus", "Opiskelija on suorittanut perusopetuksen lisäopetuksen (kymppiluokka)", lisäopetuksenPäättötodistus)
  )
}
