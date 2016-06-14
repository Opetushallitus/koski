package fi.oph.koski.api

import fi.oph.koski.documentation.{YleissivistavakoulutusExampleData, PerusopetusExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  describe("Suoritusten tila") {
    it("Todistus VALMIS ilman vahvistusta -> HTTP 400") {
      val oo: PerusopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None)))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201101 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
      }
    }

    it("Oppiainesuoritus VALMIS ilman todistuksen vahvistusta -> HTTP 400") {
      val oo: PerusopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        tila = tilaKesken,
        vahvistus = None,
        osasuoritukset = Some(List(PerusopetusExampleData.suoritus(PerusopetusExampleData.oppiaine("GE")).copy(tila = tilaValmis)))
      )))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koskioppiaineetyleissivistava/GE puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
      }
    }
  }
}