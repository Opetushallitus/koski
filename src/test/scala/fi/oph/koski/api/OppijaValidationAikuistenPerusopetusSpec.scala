package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.schema._

class OppijaValidationAikuistenPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(
    suoritukset = List(
      ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
        AikuistenPerusopetus(diaari),
        ExamplesAikuistenPerusopetus.oppiaineidenSuoritukset2017
      )
    ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-1280-2017"
}