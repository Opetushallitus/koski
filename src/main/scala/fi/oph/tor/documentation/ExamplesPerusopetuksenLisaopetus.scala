package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.YleissivistavakoulutusExampleData._
import fi.oph.tor.documentation.PerusopetusExampleData._
import fi.oph.tor.schema._

object ExamplesPerusopetuksenLisaopetus {
  private def suoritus(aine: PerusopetuksenOppiaine) = PerusopetuksenLisäopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None
  )

  private def arviointi(arvosana: String, korotus: Boolean): Some[List[PerusopetuksenLisäopetuksenOppiaineenArviointi]] = {
    Some(List(PerusopetuksenLisäopetuksenOppiaineenArviointi(arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoyleissivistava"), korotus = korotus )))
  }

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
              suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(7, true)),
              suoritus(kieli("A1", "EN")).copy(arviointi = arviointi(10, true)),
              suoritus(kieli("B1", "SV")).copy(arviointi = arviointi(6, true)),
              suoritus(oppiaine("MA")).copy(arviointi = arviointi(6, true)),
              suoritus(oppiaine("BI")).copy(arviointi = arviointi(10, true)),
              suoritus(oppiaine("GE")).copy(arviointi = arviointi(9, true)),
              suoritus(oppiaine("FY")).copy(arviointi = arviointi(8, true)),
              suoritus(oppiaine("KE")).copy(arviointi = arviointi(9, true)),
              suoritus(oppiaine("TE")).copy(arviointi = arviointi(8, true)),
              suoritus(oppiaine("HI")).copy(arviointi = arviointi(7, false)),
              suoritus(oppiaine("YH")).copy(arviointi = arviointi(8, true)),
              suoritus(oppiaine("KU")).copy(arviointi = arviointi(8, false)),
              suoritus(oppiaine("LI")).copy(arviointi = arviointi(7, true))
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
    Example("aikuisten perusopetuksen lisäopetus", "Opiskelija on suorittanut perusopetuksen lisäopetuksen (kymppiluokka)", lisäopetuksenPäättötodistus)
  )
}
