package fi.oph.koski.fixture

import fi.oph.koski.documentation.AmmatillinenExampleData.autoalanPerustutkinnonSuoritus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesLukio2019.vahvistamatonOppimääränSuoritus
import fi.oph.koski.documentation.ExamplesPerusopetus.toimintaAlueenSuoritus
import fi.oph.koski.documentation.LukioExampleData.opiskeluoikeusAktiivinen
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, perusopetus, suoritustapaErityinenTutkinto}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate

object MaksuttomuusRaporttiFixtures {
  val oppilaitos = MockOrganisaatiot.stadinAmmattiopisto
  val toimipiste = AmmatillinenExampleData.stadinToimipiste
  val alkamispäivä = LocalDate.of(2021, 8, 1)

  val maksuttomuusJakso = Maksuttomuus(alku = LocalDate.of(2021, 10, 10), loppu = None, maksuton = true)

  val maksuttomuuttaPidennettyAikaisempi = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2025, 1, 1), loppu = LocalDate.of(2025, 10, 15))
  val maksuttomuuttaPidennetty = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2025, 10, 20), loppu = LocalDate.of(2025, 10, 25))
  val maksuttomuuttaPidennettyMyöhempi = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2025, 12, 30), loppu = LocalDate.of(2026, 1, 10))

  val lisätiedotAmmatillinen = AmmatillisenOpiskeluoikeudenLisätiedot(
    hojks = None,
    maksuttomuus = Some(List(maksuttomuusJakso)),
    oikeuttaMaksuttomuuteenPidennetty = Some(List(maksuttomuuttaPidennettyAikaisempi, maksuttomuuttaPidennetty))
  )

  val lisätiedotLukio = LukionOpiskeluoikeudenLisätiedot(
    maksuttomuus = Some(List(maksuttomuusJakso)),
    oikeuttaMaksuttomuuteenPidennetty = Some(List(maksuttomuuttaPidennetty, maksuttomuuttaPidennettyMyöhempi))
  )

  val opiskeluoikeusAmmatillinenMaksuttomuuttaPidennetty = AmmatillinenOpiskeluoikeus(
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
    oppilaitos = Some(Oppilaitos(oppilaitos)),
    suoritukset = List(autoalanPerustutkinnonSuoritus(toimipiste).copy(alkamispäivä = Some(alkamispäivä.plusDays(1)))),
    lisätiedot = Some(lisätiedotAmmatillinen)
  )

  val opiskeluoikeusLukioMaksuttomuuttaPidennetty = LukionOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(alku = LocalDate.of(2021, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
      )
    ),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(vahvistamatonOppimääränSuoritus),
    lisätiedot = Some(lisätiedotLukio)
  )

  val peruskouluSuoritettu2021 = peruskouluSuoritettu(LocalDate.of(2021, 6, 4))

  val peruskouluSuoritettu2020 = peruskouluSuoritettu(LocalDate.of(2020, 12, 31))

  val peruskouluEronnut2020 = PerusopetuksenOpiskeluoikeus(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2012, 8, 15), opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2020, 12, 31), opiskeluoikeusEronnut),
    )),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      NuortenPerusopetuksenOppimääränSuoritus(
        koulutusmoduuli = perusopetus,
        suorituskieli = suomenKieli,
        toimipiste = jyväskylänNormaalikoulu,
        suoritustapa = suoritustapaErityinenTutkinto,
        osasuoritukset = Some(List(
          toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana")))),
          toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S", kuvaus = None))
        ))
      )),
    lisätiedot = None,
  )

  private def peruskouluSuoritettu(päivä: LocalDate) = PerusopetuksenOpiskeluoikeus(
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
      NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2012, 8, 15), opiskeluoikeusLäsnä),
      NuortenPerusopetuksenOpiskeluoikeusjakso(päivä, opiskeluoikeusValmistunut),
    )),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      NuortenPerusopetuksenOppimääränSuoritus(
        koulutusmoduuli = perusopetus,
        suorituskieli = suomenKieli,
        toimipiste = jyväskylänNormaalikoulu,
        vahvistus = vahvistusPaikkakunnalla(päivä),
        suoritustapa = suoritustapaErityinenTutkinto,
        osasuoritukset = Some(List(
          toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana")))),
          toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S", kuvaus = None)),
          toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S", kuvaus = None))
        ))
      )),
    lisätiedot = None,
  )
}
