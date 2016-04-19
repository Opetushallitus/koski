package fi.oph.tor.api

import fi.oph.tor.schema._

class TorOppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with OpiskeluoikeusTestMethodsPeruskoulutus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map(
    suoritus => suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
}