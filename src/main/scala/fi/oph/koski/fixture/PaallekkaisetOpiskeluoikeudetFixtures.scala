package fi.oph.koski.fixture

import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillisetTutkinnonOsat, hyväksytty, järjestämismuotoOppilaitos, järjestämismuotoOppisopimus, sosiaaliJaTerveysalanPerustutkinto, stadinAmmattiopisto, stadinToimipiste, suoritustapaNäyttö, tutkinnonOsanSuoritus}
import fi.oph.koski.documentation.ExampleData.{helsinki, suomenKieli, vahvistus}

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmattitutkintoExample, ExampleData}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeudenTila, AmmatillinenOpiskeluoikeusjakso, AmmatillisenTutkinnonSuoritus, Järjestämismuotojakso, Oppilaitos}

object PaallekkaisetOpiskeluoikeudetFixtures {

  val ensimmaisenAlkamispaiva = date(2020, 9, 1)
  val ensimmaisenPaattymispaiva = date(2020, 10, 10)
  val ensimmainenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(ensimmaisenAlkamispaiva, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(ensimmaisenPaattymispaiva, ExampleData.opiskeluoikeusEronnut, Some(ExampleData.valtionosuusRahoitteinen))
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
      AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
        suoritustapa = suoritustapaNäyttö,
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, Some(helsinki)),
        osasuoritukset = Some(List(
          tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100833", "Hoito ja huolenpito", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100834", "Kuntoutumisen tukeminen", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100840", "Lasten ja nuorten hoito ja kasvatus", ammatillisetTutkinnonOsat, hyväksytty)
        ))
      )
    ),
    arvioituPäättymispäivä = None
  )

  val viimeinenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2020, 12, 29), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2020, 12, 30), ExampleData.opiskeluoikeusValiaikaisestiKeskeytynyt, opintojenRahoitus = None),
      AmmatillinenOpiskeluoikeusjakso(date(2020, 12, 31), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    suoritukset = List(
      AmmatillinenExampleData.ympäristöalanPerustutkintoValmis().copy(
        alkamispäivä = Some(date(2020, 12, 31)),
        vahvistus = None,
        keskiarvo = None
      )
    ),
    arvioituPäättymispäivä = None
  )
}
