package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesLukioonValmistavaKoulutus
import fi.oph.koski.documentation.ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenSuoritus
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationLukioonValmistavaSpec extends TutkinnonPerusteetTest[LukioonValmistavanKoulutuksenOpiskeluoikeus] with LocalJettyHttpSpecification {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lukioonValmistavanKoulutuksenSuoritus.copy(koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus] = implicitly[TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: LukioonValmistavanKoulutuksenOpiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus
}
