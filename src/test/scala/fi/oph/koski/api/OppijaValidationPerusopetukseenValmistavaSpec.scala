package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesPerusopetukseenValmistavaOpetus.{perusopetukseenValmistavaOpiskeluoikeus, perusopetukseenValmistavanOpetuksenSuoritus}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{oppiaine, vuosiviikkotuntia}
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetukseenValmistavaSpec extends TutkinnonPerusteetTest[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] with LocalJettyHttpSpecification {
  // Jos päätetään, että päätason suorituksen diaarinumero validoidaan, niin OppijaValidationPerusopetukseenValmistavaSpec voi pistää perimään TutkinnonPerusteetTest
  // ja tämän "Tutkinnon perusteet" osion voi poistaa
  "Nuorten perusopetuksen oppiaineen suoritus valmistavassa opetuksessa" - {
    "Luokka-astetta ei vaadita jos arvionti on 'O'" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1)),
        arviointi = PerusopetusExampleData.arviointi("O")
      ))))

      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatusOk()
      }
    }
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    perusopetukseenValmistavanOpetuksenSuoritus.copy(koulutusmoduuli = perusopetukseenValmistavanOpetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  override def tag: TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = perusopetukseenValmistavaOpiskeluoikeus
}
