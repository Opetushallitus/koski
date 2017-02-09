package fi.oph.koski.api

import fi.oph.koski.documentation.{YleissivistavakoulutusExampleData, PerusopetusExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  "Suoritusten tila" - {
    "Todistus VALMIS ilman vahvistusta -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201101 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
      }
    }

    "Valmis oppiainesuoritus vaatii arvioinnin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(PerusopetusExampleData.suoritus(PerusopetusExampleData.oppiaine("GE"))))
      )))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.arviointiPuuttuu("Suoritukselta koskioppiaineetyleissivistava/GE puuttuu arviointi, vaikka suorituksen tila on VALMIS"))
      }
    }

    "Valmis oppiainesuoritus ei vaadi vahvistusta." in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(PerusopetusExampleData.suoritus(PerusopetusExampleData.oppiaine("GE")).copy(arviointi = PerusopetusExampleData.arviointi(9))))
      )))) {
        verifyResponseStatus(200)
      }
    }
  }
}