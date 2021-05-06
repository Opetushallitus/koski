package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesValma
import fi.oph.koski.documentation.ExamplesValma.valmaKoulutuksenSuoritus
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationValmaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    valmaKoulutuksenSuoritus.copy(koulutusmoduuli = valmaKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[AmmatillinenOpiskeluoikeus] = implicitly[TypeTag[AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: AmmatillinenOpiskeluoikeus = ExamplesValma.valmaOpiskeluoikeus
}
