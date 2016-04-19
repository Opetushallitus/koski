package fi.oph.tor.api

import fi.oph.tor.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class TorOppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with OpiskeluoikeusTestMethodsPeruskoulutus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map(
    suoritus => suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
}