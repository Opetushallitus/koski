package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsLukio2015
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

class OppijaValidationLukionPaikallisenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(
    LukionOppiaineenOppimääränSuoritus2015(
      koulutusmoduuli = PaikallinenLukionOppiaine2015(PaikallinenKoodi("ENA", "Englanti"), "Englanti", pakollinen = false, perusteenDiaarinumero = diaari),
      suorituskieli = suomenKieli,
      toimipiste = jyväskylänNormaalikoulu,
      osasuoritukset = None
    )
  ))

  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
}
