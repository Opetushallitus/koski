package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesLukio2019 {
  lazy val opiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        LukionOppimääränSuoritus2019(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = None
        )
      )
    )
}

