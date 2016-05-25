package fi.oph.koski.api

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsAmmatillinen extends OpiskeluOikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  override def defaultOpiskeluoikeus = opiskeluoikeus()

  val autoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", "koulutus"), Some("39/011/2014"))

  lazy val tutkintoSuoritus: AmmatillisenTutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = autoalanPerustutkinto,
    tutkintonimike = None,
    osaamisala = None,
    suoritustapa = None,
    järjestämismuoto = None,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaKesken,
    alkamispäivä = None,
    toimipiste = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste),
    arviointi = None,
    vahvistus = None,
    osasuoritukset = None
  )

  def opiskeluoikeus(suoritus: AmmatillisenTutkinnonSuoritus = tutkintoSuoritus) = AmmatillinenOpiskeluoikeus(None, None, None, None, None, None,
    oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto), None,
    suoritukset = List(suoritus),
    None, AmmatillinenExampleData.tavoiteTutkinto, None, None
  )
}
