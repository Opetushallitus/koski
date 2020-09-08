package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.{LukioExampleData, PerusopetusExampleData}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukionOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio2015 {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    LukionOppiaineenOppimääränSuoritus2015(
      koulutusmoduuli = LukionMuuValtakunnallinenOppiaine2015(Koodistokoodiviite("HI", "koskioppiaineetyleissivistava"), perusteenDiaarinumero = diaari),
      suorituskieli = suomenKieli,
      toimipiste = jyväskylänNormaalikoulu,
      osasuoritukset = None
    )
  ))

  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"

  "Ei tiedossa oppiainetta" - {
    "ei voi vahvistaa" in {
      val eitiedossa = defaultOpiskeluoikeus.copy(suoritukset = List(
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = EiTiedossaOppiaine(),
          toimipiste = jyväskylänNormaalikoulu,
          suorituskieli = suomenKieli,
          osasuoritukset = None,
          arviointi = LukioExampleData.arviointi("9"),
          vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))
        )))

      putOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tyhjänOppiaineenVahvistus(""""Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi"""))
      }
    }
  }
}
