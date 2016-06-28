package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsAmmatillinen extends PutOpiskeluOikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  override def defaultOpiskeluoikeus = opiskeluoikeus()
  val autoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("39/011/2014"))

  lazy val tutkintoSuoritus: AmmatillisenTutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = autoalanPerustutkinto,
    tutkintonimike = None,
    osaamisala = None,
    suoritustapa = None,
    järjestämismuoto = None,
    suorituskieli = None,
    tila = tilaKesken,
    alkamispäivä = None,
    toimipiste = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste),
    vahvistus = None,
    osasuoritukset = None
  )

  def opiskeluoikeus(suoritus: AmmatillisenTutkinnonSuoritus = tutkintoSuoritus) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(longTimeAgo),
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, None))),
    oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritukset = List(suoritus),
    tavoite = AmmatillinenExampleData.tavoiteTutkinto
  )
}
