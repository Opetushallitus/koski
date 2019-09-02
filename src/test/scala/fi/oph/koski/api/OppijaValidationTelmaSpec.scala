package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesTelma
import fi.oph.koski.documentation.ExamplesTelma.telmaKoulutuksenSuoritus
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationTelmaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with LocalJettyHttpSpecification {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    telmaKoulutuksenSuoritus.copy(koulutusmoduuli = telmaKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[AmmatillinenOpiskeluoikeus] = implicitly[TypeTag[AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: AmmatillinenOpiskeluoikeus = ExamplesTelma.telmaOpiskeluoikeus
}
