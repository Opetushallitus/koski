package fi.oph.koski.fixture

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.schema._

object PerusopetusOppijaMaaratRaporttiFixtures {
  val date = LocalDate.of(2012, 1, 1)

  lazy val tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
    NuortenPerusopetuksenOpiskeluoikeusjakso(alku = date.minusYears(6), tila = opiskeluoikeusLäsnä)
  ))
  lazy val aikajakso = Aikajakso(date, Some(date.plusMonths(1)))
  lazy val etp = ErityisenTuenPäätös(alku = Some(date), loppu = Some(date.plusMonths(1)), erityisryhmässä = None)

  val tavallinen = PerusopetuksenOpiskeluoikeus(
    tila = tila,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero),
        luokka = "6C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      ),
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(5, perusopetuksenDiaarinumero),
        luokka = "5C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date.minusYears(1)),
        vahvistus = vahvistusPaikkakunnalla(date),
        osasuoritukset = kaikkiAineet
      )
    ),
    lisätiedot = None
  )

  val erikois = PerusopetuksenOpiskeluoikeus(
    tila = tila,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(8, perusopetuksenDiaarinumero),
        luokka = "8C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(aikajakso),
      vaikeastiVammainen = Some(List(aikajakso)),
      erityisenTuenPäätös = Some(etp),
      majoitusetu = Some(aikajakso),
      kuljetusetu = Some(aikajakso),
      sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
      koulukoti = Some(List(aikajakso)),
      joustavaPerusopetus = Some(aikajakso)
    ))
  )

  val virheellisestiSiirrettyVaikeastiVammainen = PerusopetuksenOpiskeluoikeus(
    tila = tila,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero),
        luokka = "7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(aikajakso),
      vaikeastiVammainen = Some(List(aikajakso)),
      majoitusetu = Some(aikajakso),
      kuljetusetu = Some(aikajakso),
      sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
      koulukoti = Some(List(aikajakso)),
      joustavaPerusopetus = Some(aikajakso)
    ))
  )

  val virheellisestiSiirrettyVammainen = PerusopetuksenOpiskeluoikeus(
    tila = tila,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero),
        luokka = "7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(aikajakso),
      vammainen = Some(List(aikajakso))
    ))
  )
}
