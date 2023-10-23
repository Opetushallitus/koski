package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsLukio2015
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukionOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 {
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

      setupOppijaWithOpiskeluoikeus(eitiedossa) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tyhjänOppiaineenVahvistus(""""Ei tiedossa"-oppiainetta ei voi merkitä valmiiksi"""))
      }
    }
  }

  "oppimääräSuoritettu-kenttä" - {
    "Ei voi olla true, jos ei ole vahvistettuja päätason suorituksia" in {
      val oo = opiskeluoikeusWithPerusteenDiaarinumero(Some("60/011/2015")).copy(
        oppimääräSuoritettu = Some(true)
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.oppimääräSuoritettuIlmanVahvistettuaOppiaineenOppimäärää())
      }
    }

    "Voi olla false, jos ei ole vahvistettuja päätason suorituksia" in {
      val oo = opiskeluoikeusWithPerusteenDiaarinumero(Some("60/011/2015")).copy(
        oppimääräSuoritettu = Some(false)
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Voi olla true, jos on vahvistettuja päätason suorituksia" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(true),
        suoritukset = List(
          LukionOppiaineenOppimääränSuoritus2015(
            koulutusmoduuli = LukionMuuValtakunnallinenOppiaine2015(Koodistokoodiviite("HI", "koskioppiaineetyleissivistava"), perusteenDiaarinumero = Some("60/011/2015")),
            suorituskieli = suomenKieli,
            toimipiste = jyväskylänNormaalikoulu,
            osasuoritukset = None,
            arviointi = LukioExampleData.arviointi("9"),
            vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))
          )
        )
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }
}
