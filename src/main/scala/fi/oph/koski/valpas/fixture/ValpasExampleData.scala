package fi.oph.koski.valpas.fixture

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritusKesken, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema.{NuortenPerusopetuksenOpiskeluoikeudenTila, NuortenPerusopetuksenOpiskeluoikeusjakso, PerusopetuksenOpiskeluoikeus}

object ValpasExampleData {
  def oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
      ),
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )
}
