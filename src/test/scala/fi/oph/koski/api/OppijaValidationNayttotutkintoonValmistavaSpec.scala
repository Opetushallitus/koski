package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.AmmattitutkintoExample
import fi.oph.koski.documentation.AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus
import fi.oph.koski.schema._

class OppijaValidationNayttotutkintoonValmistavaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  override def defaultOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus))

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): AmmatillinenOpiskeluoikeus =
    AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(tutkinto = AmmattitutkintoExample.tutkinto.copy(perusteenDiaarinumero = diaari))))

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"

  "Voi merkitä valmiiksi vaikka ei sisällä osasuorituksia" in {
     putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(osasuoritukset = None)))) {
       verifyResponseStatusOk()
     }
  }
}
