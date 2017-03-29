package fi.oph.koski.api

import fi.oph.koski.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetuksenVuosiluokkaSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    vuosiluokkasuoritus.copy(koulutusmoduuli = vuosiluokkasuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
}