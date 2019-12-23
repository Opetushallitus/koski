package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

class OppijaValidationLukionPaikallisenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(
    LukionOppiaineenOppimääränSuoritus(
      koulutusmoduuli = PaikallinenLukionOppiaine(PaikallinenKoodi("ENA", "Englanti"), "Englanti", pakollinen = false, perusteenDiaarinumero = diaari),
      suorituskieli = suomenKieli,
      toimipiste = jyväskylänNormaalikoulu,
      osasuoritukset = None
    )
  ))

  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
}
