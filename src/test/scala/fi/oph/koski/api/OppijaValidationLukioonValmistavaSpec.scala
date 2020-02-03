package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesLukioonValmistavaKoulutus
import fi.oph.koski.documentation.ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenSuoritus
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import fi.oph.koski.documentation.ExampleData._
import java.time.LocalDate.{of => date}

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationLukioonValmistavaSpec extends TutkinnonPerusteetTest[LukioonValmistavanKoulutuksenOpiskeluoikeus] with LocalJettyHttpSpecification {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lukioonValmistavanKoulutuksenSuoritus.copy(koulutusmoduuli = lukioonValmistavanKoulutuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
  override def tag: TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus] = implicitly[TypeTag[LukioonValmistavanKoulutuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: LukioonValmistavanKoulutuksenOpiskeluoikeus = ExamplesLukioonValmistavaKoulutus.lukioonValmistavanKoulutuksenOpiskeluoikeus

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta lasna puuttuu opintojen rahoitus"))
      }
    }
    "valmistunut -tila vaaditaan opintojen rahoitus" in {
      val tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut))
      )
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta valmistunut puuttuu opintojen rahoitus"))
      }
    }
  }
}
