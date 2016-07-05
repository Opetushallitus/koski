package fi.oph.koski.api

import fi.oph.koski.api.OpiskeluoikeusTestMethodsLukio.päättötodistusSuoritus
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukioSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {

  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  describe("Laajuudet") {
    it("""Kurssin laajuusyksikkö muu kuin "kurssia" -> HTTP 400""") {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(oppiaine("GE", laajuus(1.0f, "4"))).copy(
          osasuoritukset = Some(List( kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "5"))) ))
        )))
      )))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*instance value .+ not found.*".r))
      }
    }
    it("Kurssien laajuuksien summa ei täsmää -> HTTP 400") {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(oppiaine("GE", laajuus(2.0f, "4"))).copy(
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "4")))
          ))
        )))
      )))
      putOpiskeluOikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen koskioppiaineetyleissivistava/GE osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 2.0"))
      }
    }
  }
}