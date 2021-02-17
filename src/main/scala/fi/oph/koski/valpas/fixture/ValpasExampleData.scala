package fi.oph.koski.valpas.fixture

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, omanÄidinkielenOpinnotSaame, perusopetuksenOppimääränSuoritusKesken, perusopetus, suoritustapaKoulutus, yhdeksännenLuokanSuoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste}
import fi.oph.koski.schema.{NuortenPerusopetuksenOpiskeluoikeudenTila, NuortenPerusopetuksenOpiskeluoikeusjakso, NuortenPerusopetuksenOppimääränSuoritus, PerusopetuksenOpiskeluoikeus}

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

  // Päällekkäiset opiskeluoikeudet esimerkit

  def oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein1 = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(kulosaarenAlaAste),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken.copy(
        toimipiste = kulosaarenAlaAste,
      ),
      kahdeksannenLuokanSuoritus.copy(
        toimipiste = kulosaarenAlaAste,
        alkamispäivä = Some(date(2019, 8, 15)),
        vahvistus = vahvistusPaikkakunnalla(date(2020, 5, 30)),
        luokka = "8A"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  def oppivelvollinenVaihtanutKouluaMuttaOpiskeluoikeusMerkkaamattaOikein2 = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      yhdeksännenLuokanSuoritus.copy(
        alkamispäivä = Some(date(2020, 8, 15)),
        vahvistus = None,
        luokka = "9B"
      )
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

}
