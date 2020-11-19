package fi.oph.koski.fixture

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmattitutkintoExample, ExampleData}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeusjakso, Oppilaitos}

object PaallekkaisetOpiskeluoikeudetFixtures {

  val ensimmaisenAlkamispaiva = date(2020, 9, 1)
  val ensimmainenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(ensimmaisenAlkamispaiva, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2020, 10, 10), ExampleData.opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(alkamispäivä = Some(date(2020, 9, 1)), vahvistus = None),
    ),
    arvioituPäättymispäivä = None
  )

  val keskimmaisenAlkamispaiva = date(2020, 10, 10)
  val keskimmainenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.stadinOppisopimuskeskus)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(keskimmaisenAlkamispaiva, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
      AmmatillinenOpiskeluoikeusjakso(date(2021, 1, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(alkamispäivä = Some(date(2020, 10, 10)), vahvistus = None),
      AmmatillinenExampleData.ympäristöalanPerustutkintoValmis().copy(
        alkamispäivä = Some(date(2020, 10, 10)),
        vahvistus = None
      )
    ),
    arvioituPäättymispäivä = None
  )

  val viimeinenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2020, 11, 11), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
    )),
    suoritukset = List(
      AmmatillinenExampleData.ympäristöalanPerustutkintoValmis().copy(
        alkamispäivä = Some(date(2020, 11, 11)),
        vahvistus = None
      )
    ),
    arvioituPäättymispäivä = None
  )
}
