package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetuksenLisäopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenLisäopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lisäopetuksenSuoritus.copy(koulutusmoduuli = lisäopetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  override def tag: TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus
}
