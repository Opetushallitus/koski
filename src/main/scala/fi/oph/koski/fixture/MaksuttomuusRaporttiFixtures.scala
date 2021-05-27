package fi.oph.koski.fixture

import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.documentation.AmmatillinenExampleData.autoalanPerustutkinnonSuoritus
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.ExamplesLukio2019.vahvistamatonOppimääränSuoritus
import fi.oph.koski.documentation.LukioExampleData.opiskeluoikeusAktiivinen
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeus, AmmatillinenOpiskeluoikeusjakso, AmmatillisenOpiskeluoikeudenLisätiedot, LukionOpiskeluoikeudenLisätiedot, LukionOpiskeluoikeudenTila, LukionOpiskeluoikeus, LukionOpiskeluoikeusjakso, Maksuttomuus, OikeuttaMaksuttomuuteenPidennetty, Oppilaitos}

import java.time.LocalDate

object MaksuttomuusRaporttiFixtures {
  val oppilaitos = MockOrganisaatiot.stadinAmmattiopisto
  val toimipiste = AmmatillinenExampleData.stadinToimipiste
  val alkamispäivä = longTimeAgo

  val maksuttomuusJakso = Maksuttomuus(alku = LocalDate.of(2020, 10, 10), loppu = None, maksuton = true)

  val maksuttomuuttaPidennettyAikaisempi = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2020, 10, 10), loppu = LocalDate.of(2020, 10, 15))
  val maksuttomuuttaPidennetty = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2020, 10, 20), loppu = LocalDate.of(2020, 10, 25))
  val maksuttomuuttaPidennettyMyöhempi = OikeuttaMaksuttomuuteenPidennetty(alku = LocalDate.of(2020, 12, 30), loppu = LocalDate.of(2021, 1, 10))

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
        LukionOpiskeluoikeusjakso(alku = LocalDate.of(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
      )
    ),
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(vahvistamatonOppimääränSuoritus),
    lisätiedot = Some(lisätiedotLukio)
  )
}
