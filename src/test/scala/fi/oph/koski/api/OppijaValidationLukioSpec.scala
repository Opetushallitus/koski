package fi.oph.koski.api

import fi.oph.koski.api.OpiskeluoikeusTestMethodsLukio.päättötodistusSuoritus
import fi.oph.koski.documentation.LukioExampleData
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukioSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"

  "Laajuudet" - {
    """Kurssin laajuusyksikkö muu kuin "kurssia" -> HTTP 400""" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(1.0f, "4"))).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List( kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "5"))) ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
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

    "Suoritus valmis, laajuudet täsmää pyöristyksillä -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(1.0f, "4"))).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(0.33333f, "4"))).copy(arviointi = numeerinenArviointi(9)),
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE2").copy(laajuus = laajuus(0.33333f, "4"))).copy(arviointi = numeerinenArviointi(9)),
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE3").copy(laajuus = laajuus(0.33333f, "4"))).copy(arviointi = numeerinenArviointi(9))
          ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Suoritus kesken, kurssien laajuuksien summa ei täsmää -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(2.0f, "4"))).copy(
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuus(1.0f, "4"))).copy(arviointi = numeerinenArviointi(9))
          ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Kaksi samaa oppiainetta" - {
    "Identtisillä tiedoilla -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
        suoritus(lukionÄidinkieli("AI1")).copy(arviointi = arviointi("9")),
        suoritus(lukionÄidinkieli("AI1")).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran"))
      }
    }
    "Eri kielivalinnalla -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
        suoritus(lukionÄidinkieli("AI1")).copy(arviointi = arviointi("9")),
        suoritus(lukionÄidinkieli("AI2")).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatusOk()
      }
    }
    "Eri matematiikan oppimäärällä -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
        suoritus(matematiikka("MAA")).copy(arviointi = arviointi("9")),
        suoritus(matematiikka("MAB")).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus koskioppiaineetyleissivistava/MA esiintyy useammin kuin kerran"))
      }
    }
  }

  "Tilat ja vahvistukset" - {
    "Valmis oppiainesuoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE")).copy(arviointi = arviointi("9"))))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
    "Valmis oppiaineen kurssin suoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(
          suoritus(lukionOppiaine("GE")).copy(arviointi = arviointi("9")).copy(
            osasuoritukset = Some(List(
              kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1")).copy(arviointi = numeerinenArviointi(9))
            ))
          )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }
}
