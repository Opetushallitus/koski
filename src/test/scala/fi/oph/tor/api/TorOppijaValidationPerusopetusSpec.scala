package fi.oph.tor.api

import fi.oph.tor.documentation.{YleissivistavakoulutusExampleData, PerusopetusExampleData}
import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.schema._

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class TorOppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map(
    suoritus => suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))

  describe("Suoritusten tila") {
    it("Todistus VALMIS ilman vahvistusta -> HTTP 400") {
      val oo: PerusopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map(_.copy(vahvistus = None)))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201100 puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
      }
    }

    it("Oppiainesuoritus VALMIS ilman todistuksen vahvistusta -> HTTP 400") {
      val oo: PerusopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(suoritukset = defaultOpiskeluoikeus.suoritukset.map(_.copy(
        tila = tilaKesken,
        vahvistus = None,
        osasuoritukset = Some(List(PerusopetusExampleData.suoritus(PerusopetusExampleData.oppiaine("GE")).copy(tila = tilaValmis)))
      )))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koskioppiaineetyleissivistava/GE puuttuu vahvistus, vaikka suorituksen tila on VALMIS"))
      }
    }
  }
}