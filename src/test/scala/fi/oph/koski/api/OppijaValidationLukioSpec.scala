package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.TestMethodsLukio.päättötodistusSuoritus
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesLukio2019.{lops2019AikuistenPerusteenDiaarinumero, lops2019perusteenDiaarinumero}
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.{ExamplesLukio, LukioExampleData}
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationLukioSpec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "33/011/2003"

  "Laajuudet" - {
    """Kurssin laajuusyksikkö muu kuin "kurssia" -> HTTP 400""" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(1.0f, "4"), None)).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List( kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = Some(laajuus(1.0f, "5")))) ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
      }
    }
    "Suoritus valmis, kurssien laajuuksien summa ei täsmää -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(2.0f, "4"), None)).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = Some(laajuus(1.0f, "4")))).copy(arviointi = numeerinenArviointi(9))
          ))
        )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma("Suorituksen koskioppiaineetyleissivistava/GE osasuoritusten laajuuksien summa 1.0 ei vastaa suorituksen laajuutta 2.0"))
      }
    }

    "Suoritus valmis, laajuudet täsmää pyöristyksillä -> HTTP 200" in {
      val laajuusKursseissa = Some(laajuus(0.33333f, "4"))
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(1.0f, "4"), None)).copy(
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = laajuusKursseissa)).copy(arviointi = numeerinenArviointi(9)),
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE2").copy(laajuus = laajuusKursseissa)).copy(arviointi = numeerinenArviointi(9)),
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE3").copy(laajuus = laajuusKursseissa)).copy(arviointi = numeerinenArviointi(9))
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
        osasuoritukset = Some(List(suoritus(lukionOppiaine("GE", laajuus(2.0f, "4"), None)).copy(
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1").copy(laajuus = Some(laajuus(1.0f, "4")))).copy(arviointi = numeerinenArviointi(9))
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
        suoritus(LukioExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = arviointi("9")),
        suoritus(LukioExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran"))
      }
    }
    "Eri kielivalinnalla -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
        suoritus(LukioExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = arviointi("9")),
        suoritus(LukioExampleData.lukionÄidinkieli("AI2", pakollinen = true)).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatusOk()
      }
    }
    "Eri matematiikan oppimäärällä -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(osasuoritukset = Some(List(
        suoritus(LukioExampleData.matematiikka("MAA", None)).copy(arviointi = arviointi("9")),
        suoritus(LukioExampleData.matematiikka("MAB", None)).copy(arviointi = arviointi("9"))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus koskioppiaineetyleissivistava/MA esiintyy useammin kuin kerran"))
      }
    }
  }

  "Tilat ja vahvistukset" - {
    "Valmis oppiainesuoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(LukioExampleData.lukionOppiaine("GE", None)).copy(arviointi = arviointi("9"))))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
    "Valmis oppiaineen kurssin suoritus ei vaadi vahvistusta." in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(
          suoritus(LukioExampleData.lukionOppiaine("GE", None)).copy(arviointi = arviointi("9")).copy(
            osasuoritukset = Some(List(
              kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("GE1")).copy(arviointi = numeerinenArviointi(9))
            ))
          )))
      )))
      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
    "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
        )),
        suoritukset = List(päättötodistusSuoritus.copy(
          vahvistus = vahvistusPaikkakunnalla(date(2016, 6, 4)),
          osasuoritukset = Some(List(
            suoritus(LukioExampleData.lukionÄidinkieli("AI1", laajuus(1.0f, "4"), pakollinen = true)).copy(arviointi = arviointi("9"))
          ))
        )))
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
      }
    }
  }

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
      }
    }
    "valmistunut -tila vaaditaan opintojen rahoitus" in {
      val tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2016, 6, 8), opiskeluoikeusValmistunut))
      )
      putOpiskeluoikeus(ExamplesLukio.päättötodistus().copy(tila = tila)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta valmistunut puuttuu rahoitusmuoto"))
      }
    }
  }

  "Diaarinumero" - {
    "Lukion oppimäärässä" - {
      "Lukion 2019 opetussuunnitelman nuorten diaarinumeron käyttöä ei sallita" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
          koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = Some("OPH-2263-2019")),
          osasuoritukset = Some(List(
            suoritus(LukioExampleData.lukionÄidinkieli("AI1", laajuus(1.0f, "4"), pakollinen = true)).copy(arviointi = arviointi("9"))
          ))
        )))) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
        }
      }

      "Lukion 2019 opetussuunnitelman aikuisten diaarinumeron käyttöä ei sallita" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
          koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = lops2019AikuistenPerusteenDiaarinumero),
          osasuoritukset = Some(List(
            suoritus(LukioExampleData.lukionÄidinkieli("AI1", laajuus(1.0f, "4"), pakollinen = true)).copy(arviointi = arviointi("9"))
          ))
        )))) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
        }
      }
    }
    "Lukion oppiaineen oppimäärässä" - {
      "Lukion 2019 opetussuunnitelman nuorten diaarinumeron käyttöä ei sallita" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi.copy(koulutusmoduuli = lukionOppiaine("YH", diaarinumero = lops2019perusteenDiaarinumero))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("Lukion aiemman opetusohjelman mukaisessa suorituksessa ei voi käyttää lukion 2019 opetussuunnitelman diaarinumeroa"))
        }
      }

      "Lukion 2019 opetussuunnitelman aikuisten diaarinumeron käyttöä ei sallita" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi.copy(koulutusmoduuli = lukionOppiaine("YH", diaarinumero = lops2019AikuistenPerusteenDiaarinumero))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("Lukion aiemman opetusohjelman mukaisessa suorituksessa ei voi käyttää lukion 2019 opetussuunnitelman diaarinumeroa"))
        }
      }
    }
  }

  "Historian kurssien konversiot kun perusteen diaarinumero '70/011/2015' (Aikuisten lukiokoulutuksen opetussuunnitelman perusteet)" - {
    "Oppimäärä, perusteen diaarina '70/011/2015'" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(
        vahvistus = None,
        osasuoritukset = Some(List(suoritus(lukionOppiaine("HI", laajuus(2.0f), None)).copy(
          osasuoritukset = Some(List(
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("HI2")),
            kurssisuoritus(LukioExampleData.valtakunnallinenKurssi("HI3"))
          ))
        ))),
        koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(
          perusteenDiaarinumero = Some("70/011/2015")
        )
      )))
      val result = putAndGetOpiskeluoikeus(oo)
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should equal ("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should equal ("Kansainväliset suhteet")
    }

    "Aineopiskelija, perusteen diaarina '70/011/2015'" in {
      val suoritus = ExamplesLukio.aineopiskelija.suoritukset.head.asInstanceOf[LukionOppiaineenOppimääränSuoritus2015]
      val koulutusmoduuli = suoritus.koulutusmoduuli.asInstanceOf[LukionMuuValtakunnallinenOppiaine2015].copy(
        perusteenDiaarinumero = Some("70/011/2015")
      )
      val oo = ExamplesLukio.aineopiskelija.copy(
        suoritukset = List(suoritus.copy(
          koulutusmoduuli = koulutusmoduuli
        ))
      )
      val result = putAndGetOpiskeluoikeus(oo)
      result.suoritukset.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should equal ("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should equal ("Kansainväliset suhteet")
    }

    "Perusteen diaarinumerona jokin muu kuin '70/011/2015'; Konversiota ei tehdä" in {
      val result = putAndGetOpiskeluoikeus(ExamplesLukio.aineopiskelija)
      result.suoritukset.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should not equal ("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should not equal ("Kansainväliset suhteet")
    }
  }

  "Äidinkielen omainen oppiaine" - {
    def verify[A](kieli: String)(expect: => A): A = {
      val oo = defaultOpiskeluoikeus.copy(
        suoritukset = List(päättötodistusSuoritus.copy(
          osasuoritukset = Some(List(suoritus(LukioExampleData.lukionKieli("AOM", kieli))))
        ))
      )

      putOpiskeluoikeus(oo) {
        expect
      }
    }

    "FI sallittu" in {
      verify("FI") {
        verifyResponseStatusOk()
      }
    }
    "SV sallittu" in {
      verify("SV") {
        verifyResponseStatusOk()
      }
    }
    "Muita ei sallita" in {
      verify("SE") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi("Äidinkielen omaisen oppiaineen kieli tulee olla suomi tai ruotsi"))
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: LukionOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }
}
