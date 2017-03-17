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

  "Laajuudet" - {
    """Kurssin laajuusyksikkö muu kuin "kurssia" -> HTTP 400""" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(1.0f, "4"))).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List( kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "5"))) ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.jsonSchema(".*instance value .+ not found.*".r))
      }
    }
    "Suoritus valmis, kurssien laajuuksien summa ei täsmää -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(2.0f, "4"))).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "4"))).copy(arviointi = numeerinenArviointi(9))
          ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen koskioppiaineetyleissivistava/GE osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 2.0"))
      }
    }

    "Suoritus kesken, kurssien laajuuksien summa ei täsmää -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        tila = tilaKesken,
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(2.0f, "4"))).copy(
          tila = tilaKesken,
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "4"))).copy(arviointi = numeerinenArviointi(9))
          ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(200)
      }
    }
  }

  "Tilat ja vahvistukset" - {
    "Valmis oppiainesuoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        tila = tilaKesken,
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE")).copy(tila = tilaValmis, arviointi = arviointi("9"))))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(200)
      }
    }
    "Valmis oppiaineen kurssin suoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        tila = tilaKesken,
        vahvistus = None,
        osasuoritukset = Some(List(
          suoritus(lukionOppiaine("GE")).copy(tila = tilaValmis, arviointi = arviointi("9")).copy(
            osasuoritukset = Some(List(
              kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1")).copy(tila = tilaValmis, arviointi = numeerinenArviointi(9))
            ))
          )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(200)
      }
    }
  }
}