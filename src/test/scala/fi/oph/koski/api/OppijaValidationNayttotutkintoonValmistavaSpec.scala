package fi.oph.koski.api

import fi.oph.koski.documentation.AmmattitutkintoExample
import fi.oph.koski.schema._

class OppijaValidationNayttotutkintoonValmistavaSpec extends TutkinnonPerusteetTest[AmmatillinenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  override def defaultOpiskeluoikeus = AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus))

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): AmmatillinenOpiskeluoikeus =
    AmmattitutkintoExample.opiskeluoikeus.copy(suoritukset = List(AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(tutkinto = AmmattitutkintoExample.tutkinto.copy(perusteenDiaarinumero = diaari))))

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "13/011/2009"
  override def vääräntyyppisenPerusteenDiaarinumero: String = "60/011/2015"
}
