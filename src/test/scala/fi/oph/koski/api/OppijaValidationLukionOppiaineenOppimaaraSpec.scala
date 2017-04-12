package fi.oph.koski.api

import fi.oph.koski.schema._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukionOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    LukionOppiaineenOppimääränSuoritus(
      koulutusmoduuli = LukionMuuValtakunnallinenOppiaine(Koodistokoodiviite("HI", "koskioppiaineetyleissivistava"), perusteenDiaarinumero = diaari),
      suorituskieli = suomenKieli,
      tila = tilaKesken,
      toimipiste = jyväskylänNormaalikoulu,
      osasuoritukset = None
    )
  ))

  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"
}