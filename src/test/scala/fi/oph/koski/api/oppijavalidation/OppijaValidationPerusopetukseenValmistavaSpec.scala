package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExamplesPerusopetukseenValmistavaOpetus.{perusopetukseenValmistavaOpiskeluoikeus, perusopetukseenValmistavanOpetuksenSuoritus}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{arviointi, oppiaine, vuosiviikkotuntia}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetukseenValmistavaSpec extends TutkinnonPerusteetTest[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] with KoskiHttpSpec {
  "Nuorten perusopetuksen oppiaineen suoritus valmistavassa opetuksessa" - {
    "Luokka-astetta ei vaadita jos arvionti on 'O'" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1)),
        arviointi = PerusopetusExampleData.arviointi("O", kuvaus = None)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatusOk()
      }
    }

    "Laajuus on pakollinen" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(laajuus = None),
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        arviointi = arviointi(9)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/FY laajuus puuttuu"))
      }
    }

    "Laajuus ei pakollinen kun suoritustapa 'erityinentutkinto'" in {
      val suoritus = perusopetukseenValmistavanOpetuksenSuoritus.copy(osasuoritukset = Option(List(NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
        koulutusmoduuli = oppiaine("FY").copy(laajuus = None),
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        arviointi = arviointi(9),
        suoritustapa = Some(PerusopetusExampleData.suoritustapaErityinenTutkinto)
      ))))

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(suoritus))) {
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
