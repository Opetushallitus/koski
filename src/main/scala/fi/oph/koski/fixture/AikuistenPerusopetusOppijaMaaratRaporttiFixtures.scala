package fi.oph.koski.fixture

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.schema._

object AikuistenPerusopetusOppijaMaaratRaporttiFixtures {
  private val date = LocalDate.of(2012, 1, 1)

  private val tilaLäsnä = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
    AikuistenPerusopetuksenOpiskeluoikeusjakso(alku = date.minusYears(6), tila = opiskeluoikeusLäsnä, opintojenRahoitus = Some(valtionosuusRahoitteinen))
  ))

  private val tilaLäsnäMuuKuinVos = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
    AikuistenPerusopetuksenOpiskeluoikeusjakso(alku = date.minusYears(6), tila = opiskeluoikeusLäsnä, opintojenRahoitus = Some(muutaKauttaRahoitettu))
  ))

  val oppimääränSuorittaja = AikuistenPerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      AikuistenPerusopetuksenOppimääränSuoritus(
        koulutusmoduuli = AikuistenPerusopetus(Some("19/011/2015")),
        luokka = Some("6C"),
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        suoritustapa = suoritustapaErityinenTutkinto
      )
    ),
    lisätiedot = None
  )

  val oppimääränSuorittajaMuuKuinVos = AikuistenPerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnäMuuKuinVos,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      AikuistenPerusopetuksenOppimääränSuoritus(
        koulutusmoduuli = AikuistenPerusopetus(Some("19/011/2015")),
        luokka = Some("6C"),
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        suoritustapa = suoritustapaErityinenTutkinto,
      )
    ),
    lisätiedot = None
  )

  val aineOpiskelijaMuuKuinVos = AikuistenPerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnäMuuKuinVos,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      AikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = MuuAikuistenPerusopetuksenOppiaine(Koodistokoodiviite("MA", "koskioppiaineetyleissivistava"), perusteenDiaarinumero = Some("19/011/2015")),
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        suoritustapa = suoritustapaErityinenTutkinto,
      )
    ),
    lisätiedot = None
  )
}
