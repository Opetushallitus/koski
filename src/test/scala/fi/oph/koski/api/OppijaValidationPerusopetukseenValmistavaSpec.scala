package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesPerusopetukseenValmistavaOpetus.{perusopetukseenValmistavaOpiskeluoikeus, perusopetukseenValmistavanOpetuksenSuoritus}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.PerusopetusExampleData.{oppiaine, vuosiviikkotuntia}
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetukseenValmistavaSpec extends FreeSpec with LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] {
  // Jos päätetään, että päätason suorituksen diaarinumero validoidaan, niin OppijaValidationPerusopetukseenValmistavaSpec voi pistää perimään TutkinnonPerusteetTest
  // ja tämän "Tutkinnon perusteet" osion voi poistaa
  "Tutkinnon perusteet" - {
    "Valideilla tiedoilla" - {
      "palautetaan HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
      "myös ePerusteista löytymätön, mutta koodistosta \"koskikoulutusdiaarinumerot\" löytyvä diaarinumero kelpaa" in {
        putOpiskeluoikeus(opiskeluoikeusWithPerusteenDiaarinumero(Some(eperusteistaLöytymätönValidiDiaarinumero))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Kun yritetään liittää suoritus tuntemattomaan tutkinnon perusteeseen" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some("39/xxx/2014"))) (verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuntematonDiaari("Tutkinnon perustetta ei löydy diaarinumerolla 39/xxx/2014")))
      }
    }

    "Kun yritetään lisätä opiskeluoikeus tyhjällä diaarinumerolla" - {
      "palautetaan HTTP 400 virhe"  in {
        putTodistus(opiskeluoikeusWithPerusteenDiaarinumero(Some(""))) (verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*perusteenDiaarinumero.*".r)))
      }
    }
  }


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

  def putTodistus[A](opiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(JsonSerializer.serializeWithRoot(opiskeluoikeus)(tag))), headers)(f)
  }

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    perusopetukseenValmistavanOpetuksenSuoritus.copy(koulutusmoduuli = perusopetukseenValmistavanOpetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  def vääräntyyppisenPerusteenDiaarinumero: String = "39/011/2014"

  override def tag: TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = perusopetukseenValmistavaOpiskeluoikeus

}
