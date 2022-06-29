package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.TestMethodsLukio.päättötodistusSuoritus
import fi.oph.koski.documentation.ExampleData.{englanti, opiskeluoikeusEronnut, opiskeluoikeusLäsnä, ruotsinKieli, suomenKieli, vahvistusPaikkakunnalla, valtionosuusRahoitteinen}
import fi.oph.koski.documentation.ExamplesLukio2019.{aikuistenOppiaineidenOppimäärienSuoritus, aikuistenOppimäärienSuoritus, aktiivinenOpiskeluoikeus, oppiaineidenOppimäärienSuoritus, oppimääränSuoritus, vahvistamatonOppimääränSuoritus}
import fi.oph.koski.documentation.Lukio2019ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt}
import fi.oph.koski.documentation.{ExampleData, ExamplesLukio, ExamplesLukio2019, Lukio2019ExampleData, LukioExampleData}
import fi.oph.koski.http.ErrorMatcher.exact
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationLukio2019Spec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[LukionOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio {
  "Laajuudet" - {
    "Oppiaineen laajuus" - {
      "Oppiaineen laajuus lasketaan moduuleiden ja paikallisten opintojaksojen laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1").copy(laajuus = laajuus(2.5))).copy(arviointi = numeerinenArviointi(8)),
              paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT231", "Tanssin alkeet", "Rytmissä pysyminen").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7)),
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(laajuus = laajuus(0.5))).copy(arviointi = numeerinenArviointi(8))
            )))
          ) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail)
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Muiden opintojen laajuus lasketaan moduuleiden ja paikallisten opintojaksojen laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
              moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("ÄI1").copy(laajuus = laajuus(2.5))).copy(arviointi = numeerinenArviointi(8)),
              paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT231", "Tanssin alkeet", "Rytmissä pysyminen").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7)),
              moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("ÄI3").copy(laajuus = laajuus(0.5))).copy(arviointi = numeerinenArviointi(8))
            )))
          ) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail)
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Jos oppiaineella ei ole osasuorituksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true).copy(laajuus = Some(laajuus(9.0)))).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
          ) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail)
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Jos muilla suorituksilla ei ole osasuorituksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            muidenLukioOpintojenSuoritus(lukiodiplomit().copy(laajuus = Some(laajuus(9.0)))).copy(osasuoritukset = None)
          ) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail)
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Moduulin oletuslaajuus on 2" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3")).copy(arviointi = numeerinenArviointi(8))
          )))) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail)
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(6.0)
      }

      "Jos oppiaineella ei ole osasuorituksia laajuus on 0" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(4)).copy(osasuoritukset = None)
          ) ::: oppiainesuorituksetRiittääValmistumiseenNuorilla.tail))))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(0)
      }
    }

    "vahvistuksessa" - {
      "Yli 150 op ja yli 20 op valinnaisia sisältävän nuorten oppimäärän suorituksen pystyy vahvistamaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(oppiainesuorituksetRiittääValmistumiseenNuorilla))))) {
          verifyResponseStatusOk()
        }
      }

      "Alle 150 op sisältävän nuorten oppimäärän suorituksen pystyy siirtämään vahvistamatta" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )),
          suoritukset = List(oppimääränSuoritus.copy(vahvistus = None, osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen))))) {
          verifyResponseStatusOk()
        }
      }


      "Alle 150 op sisältävää nuorten oppimäärän suoritusta ei pysty vahvistamaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen))))) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
              s"Suoritus koulutus/309902 on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys"
            )
          )
        }
      }

      "Yli 150 op sisältävää nuorten oppimäärän suoritusta, jossa ei ole vähintään 20 op valinnaisia suorituksia, ei pysty vahvistamaan" in {
        val osasuoritukset = oppiainesuorituksetRiittääValmistumiseenAikuisilla ::: List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(4)),
          oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike")).copy(arviointi = numeerinenLukionOppiaineenArviointi(8)).copy(osasuoritukset = Some(List(
            paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT231", "Tanssin alkeet", "Rytmissä pysyminen")).copy(arviointi = numeerinenArviointi(7)),
            paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne").copy(laajuus = laajuus(50))).copy(arviointi = numeerinenArviointi(10))
          ))))

        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(osasuoritukset))))) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
              s"Suoritus koulutus/309902 on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys"
            )
          )
        }
      }

      "Yli 88 op sisältävän aikuisten oppimäärän suorituksen pystyy vahvistamaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = ExamplesLukio2019.lops2019AikuistenPerusteenDiaarinumero),
          oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma,
          osasuoritukset = Some(oppiainesuorituksetRiittääValmistumiseenAikuisilla)
        )))) {
          verifyResponseStatusOk()
        }
      }

      "Alle 88 op sisältävän aikuisten oppimäärän suorituksen pystyy siirtämään vahvistamatta" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = ExamplesLukio2019.lops2019AikuistenPerusteenDiaarinumero),
            oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma,
            osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen)
          ))
        )) {
          verifyResponseStatusOk()
        }
      }

      "Alle 88 op sisältävää aikuisten oppimäärän suoritusta ei pysty vahvistamaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = ExamplesLukio2019.lops2019AikuistenPerusteenDiaarinumero),
          oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma,
          osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen)
        )))) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
              s"Suoritus koulutus/309902 on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 88 op osasuorituksia, tai opiskeluoikeudelta puuttuu linkitys"
            )
          )
        }
      }
    }
  }

  "Diaarinumerot" - {
    "Nuorten ops" - {
      "Vain peruste OPH-2263-2019 sallitaan" in {
        val oppimääräAikuistenPerusteella = oppimääränSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2267-2019"))
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(koulutusmoduuli = oppimääräAikuistenPerusteella)))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2267-2019" suorituksella lukionoppimaara, sallitut arvot: OPH-2263-2019"""))
        }
      }
    }

    "Aikuisten ops" - {
      "Vain peruste OPH-2267-2019 sallitaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma)))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2263-2019" suorituksella lukionoppimaara, sallitut arvot: OPH-2267-2019"""))
        }
      }
    }
  }

  "Vahvistus ja valmistuminen lukion oppimäärien suorituksessa" - {
    "Suorituksen vahvistus tyhjennetään tietojen siirrossa" in {
      val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 5, 15))))))

      opiskeluoikeus.suoritukset.head.vahvistus should equal(None)
    }

    "Opiskeluoikeuden voi merkitä valmistuneeksi, jos siinä on arvioituja ja arvioimattomia osasuorituksia" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(8)),
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeutta ei voi merkitä valmistuneeksi, jos siinä ei ole yhtään osasuoritusta" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = None)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("""Lukion oppiaineiden oppimäärien suorituksen 2019 sisältävää opiskeluoikeutta ei voi merkitä valmiiksi ilman arvioitua oppiaineen osasuoritusta"""))
      }
    }

    "Opiskeluoikeutta ei voi merkitä valmistuneeksi, jos siinä on 2 arvioimatonta osasuoritusta" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(8)),
        )))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("""Lukion oppiaineiden oppimäärien suorituksen 2019 sisältävää opiskeluoikeutta ei voi merkitä valmiiksi ilman arvioitua oppiaineen osasuoritusta"""))
      }
    }
  }

  "Merkintä erityisestä tutkinnosta" - {
    "oppiainetasolla sallii oppiaineen ilman osasuorituksia ja suorituksen merkitsemisen vahvistetuksi vajaalla yhteislaajuudella" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(suoritettuErityisenäTutkintona = true, arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB4")).copy(arviointi = numeerinenArviointi(10))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "oppiainetasolla estää moduulin ja paikallisen opintojakson lisäämisen kyseiseen oppiaineeseen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(suoritettuErityisenäTutkintona = true, arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB4")).copy(arviointi = numeerinenArviointi(10))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI1 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI2 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI3 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus FY123 (Keittiöfysiikka) ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona")
          ))
      }
    }

    "suoritustasolla sallii oppiaineet ilman osasuorituksia ja suorituksen merkitsemisen vahvistetuksi vajaalla yhteislaajuudella" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None))))))) {
        verifyResponseStatusOk()
      }
    }

    "suoritustasolla sallii osasuoritukset muissa lukio-opinnoissa, temaattisissa opinnoissa tai lukiodiplomeissa ja suorituksen merkitsemisen vahvistetuksi vajaalla yhteislaajuudella" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KE3")).copy(arviointi = numeerinenArviointi(10)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("HAI765", "Kansanmusiikki haitarilla", "Kansamusiikkia 2-rivisellä haitarilla")).copy(arviointi = sanallinenArviointi("S"))
        ))),
        temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S"))
        ))),
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(9))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "suoritustasolla estää osasuoritusten lisäämisen oppiaineisiin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3")).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB4")).copy(arviointi = numeerinenArviointi(10)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10)),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI1 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI2 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/ÄI3 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/MAB2 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/MAB3 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/MAB4 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus FY123 (Keittiöfysiikka) ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona")
          ))
      }
    }
  }

  "Osasuoritustyypit" - {
    "Temaattisiin opintoihin ei voi siirtää moduuleita" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S")),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
        )))
      )))))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
      }
    }

    "Kaikki lukiodiplomimoduulit voi siirtää lukiodiplomeihin" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Lukiodiplomeihin ei voi siirtää paikallisia opintojaksoja tai muita kuin lukiodiplomimoduuleita" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S")),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuoritus KAN200 (Kanteleensoiton perusteet) ei ole sallittu lukiodiplomisuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuoritus moduulikoodistolops2021/MAB2 ei ole sallittu lukiodiplomisuoritus")
          ))
      }
    }

    "Lukiodiplomimoduulien laajuus ei voi olla muuta kuin 2 opintopistettä" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KOLD1", 1.0f)).copy(arviointi = numeerinenArviointi(5)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KULD2", 2.5f)).copy(arviointi = numeerinenArviointi(6)),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä, "Osasuorituksen moduulikoodistolops2021/KOLD1 laajuus ei ole oikea. Lukiodiplomimoduulin laajuus tulee olla aina 2 opintopistettä."),
            exact(KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä, "Osasuorituksen moduulikoodistolops2021/KULD2 laajuus ei ole oikea. Lukiodiplomimoduulin laajuus tulee olla aina 2 opintopistettä.")
          ))
      }
    }

    "Oppiainespesifit lukiodiplomit voi siirtää kyseisiin oppiaineisiin" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("LI")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("MU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("TE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Yleisesti lukiodiplomimoduuleita ei voi siirtää oppiaineisiin tai muihin suorituksiin" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        ))),
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KOLD1) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KULD2) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KÄLD3) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/LILD4) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/MELD5) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/MULD6) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/TALD7) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/TELD8) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),

            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KOLD1) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KULD2) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/KÄLD3) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/LILD4) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/MELD5) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/MULD6) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/TALD7) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomimoduuli (moduulikoodistolops2021/TELD8) ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus."),
          )
        )
      }
    }
  }

  "Suorituskieli" - {
    "Ei saa olla oppiainetasolla sama kuin päätason suorituksessa" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
          suorituskieli = Some(englanti),
          arviointi = numeerinenLukionOppiaineenArviointi(9),
          osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(
          suorituskieli = Some(suomenKieli),
          arviointi = numeerinenLukionOppiaineenArviointi(9),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3")).copy(arviointi = numeerinenArviointi(10)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB4")).copy(arviointi = numeerinenArviointi(10))
          ))
        )
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia("""Oppiaineen koskioppiaineetyleissivistava/MA suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli"""))
      }
    }

    "Ei saa olla moduulitasolla sama kuin oppiaineessa tai päätason suorituksessa" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
          suorituskieli = Some(englanti),
          arviointi = numeerinenLukionOppiaineenArviointi(9),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(suorituskieli = Some(ruotsinKieli), arviointi = numeerinenArviointi(10)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(suorituskieli = Some(suomenKieli), arviointi = numeerinenArviointi(10)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3")).copy(suorituskieli = Some(englanti), arviointi = numeerinenArviointi(10)),
            paikallisenOpintojaksonSuoritus(paikallinenOpintojakso(
              "KAN200",
              "Kanteleensoiton perusteet",
              "Itäsuomalaisen kanteleensoiton perusteet")
            ).copy(suorituskieli = Some(ruotsinKieli), arviointi = sanallinenArviointi("S")),
            paikallisenOpintojaksonSuoritus(paikallinenOpintojakso(
              "KAN201",
              "Kanteleensoiton jatkokurssi", "Itäsuomalaisen kanteleensoiton jatkokurssi")
            ).copy(suorituskieli = Some(suomenKieli), arviointi = sanallinenArviointi("S")),
            paikallisenOpintojaksonSuoritus(paikallinenOpintojakso(
              "KAN202", "Kanteleensoiton jatkokurssi 2",
              "Itäsuomalaisen kanteleensoiton jatkokurssi 2")
            ).copy(suorituskieli = Some(englanti), arviointi = sanallinenArviointi("S")),
          ))
        )
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuorituksen moduulikoodistolops2021/ÄI3 suorituskieli ei saa olla sama kuin oppiaineen suorituskieli"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuorituksen KAN202 (Kanteleensoiton jatkokurssi 2) suorituskieli ei saa olla sama kuin oppiaineen suorituskieli")
          )
        )
      }
    }
  }

  "Oppiaineen arvosana" - {
    "Saa olla S, jos laajuus on 2 tai alle" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(
          arviointi = sanallinenLukionOppiaineenArviointi("S"),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE1", 2.0f)).copy(arviointi = numeerinenArviointi(7))
          ))
        ))))))) {
        verifyResponseStatusOk()
      }
    }
    "Saa olla S paikallisisssa oppiaineissa ja moduuleissaa, vaikka laajuus on yli 2" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike")).copy(arviointi = sanallinenLukionOppiaineenArviointi("S")).copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne").copy(laajuus = laajuus(50), pakollinen = false)).copy(arviointi = sanallinenArviointi("S")),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT235", "Tanssin taito 2", "Uudemmat suomalaiset tanssit").copy(laajuus = laajuus(2))).copy(arviointi = sanallinenArviointi("S"))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }
    "Ei saa olla S, jos laajuus on yli 2 op" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(
          arviointi = sanallinenLukionOppiaineenArviointi("S"),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE1", 3.0f)).copy(arviointi = numeerinenArviointi(7))
          ))
        ))))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle("Oppiaineen koskioppiaineetyleissivistava/KE arvosanan pitää olla numero, jos oppiaineen laajuus on yli 2 op"))
      }
    }
    "Ei saa olla H, vaikka laajus olisi 2 op tai alle" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(
          arviointi = sanallinenLukionOppiaineenArviointi("H"),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE1", 2.0f)).copy(arviointi = numeerinenArviointi(7))
          ))
        ))))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle("Oppiaineen koskioppiaineetyleissivistava/KE arvosanan pitää olla numero"))
      }
    }
    "Liikunnassa" - {
      "Saa olla S, vaikka laajuus olisi yli 2 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("LI")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LI1", 3.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Viimeisin ei saa olla H, jos laajuus yli 2 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("LI")).copy(
            arviointi = Some(List(
              NumeerinenLukionOppiaineenArviointi2019("7"),
              SanallinenLukionOppiaineenArviointi2019("H"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LI1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LI2", 2.0f)).copy(arviointi = numeerinenArviointi(6))
            ))
          )
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle("Oppiaineen koskioppiaineetyleissivistava/LI arvosanan pitää olla numero"))
        }
      }
    }

    "Opinto-ohjauksessa" - {
      "Ei saa olla numeroarviointia" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = numeerinenLukionOppiaineenArviointi(5),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 2.0f)).copy(arviointi = sanallinenArviointi("S"))
            ))
          ))))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana("Opinto-ohjauksen oppiaineen koulutus/309902 arvosanan on oltava S tai H"))
        }
      }

      "Saa olla sanallinen arviointi S, riippumatta laajudesta" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = sanallinenLukionOppiaineenArviointi("S"),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 2.0f)).copy(arviointi = sanallinenArviointi("S")),
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP2", 3.0f)).copy(arviointi = sanallinenArviointi("S"))
            ))
          ))))))) {
          verifyResponseStatusOk()
        }
      }

      "Saa olla sanallinen arviointi H, riippumatta laajudesta" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = sanallinenLukionOppiaineenArviointi("H"),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 2.0f)).copy(arviointi = sanallinenArviointi("S")),
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP2", 3.0f)).copy(arviointi = sanallinenArviointi("S"))
            ))
          ))))))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Vieraassa kielessä" - {
      "Pakollisessa saa olla S, jos laajuus on 2 op tai alle" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = true)).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Pakollisessa aiempi saa olla S, vaikka laajuus olisi yli 2 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = true)).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"),
              NumeerinenLukionOppiaineenArviointi2019("7"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 4.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Valinnaisessa saa olla S, jos laajuus on korkeintaan 4 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB22", 2.0f)).copy(arviointi = numeerinenArviointi(6))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Valinnaisessa aiempi saa olla H, vaikka laajuus olisi yli 4 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("H"),
              NumeerinenLukionOppiaineenArviointi2019("7"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB22", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB23", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Valinnaisessa viimeisin ei saa olla H, jos laajuus yli 4 op" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
            arviointi = Some(List(
              NumeerinenLukionOppiaineenArviointi2019("7"),
              SanallinenLukionOppiaineenArviointi2019("H"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB22", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB23", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
            ))
          )
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle("""Valinnaisen vieraan kielen oppiaineen koskioppiaineetyleissivistava/B2 arvosanan pitää olla numero, jos oppiaineen laajuus on yli 4 op"""))
        }
      }
    }
  }

  "Moduulin tai paikallisen opintojakson arvosana" - {
    "Saa olla kirjain opinto-ohjauksessa" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
          arviointi = sanallinenLukionOppiaineenArviointi("S"),
          osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1")).copy(arviointi = sanallinenArviointi("S")),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP2")).copy(arviointi = sanallinenArviointi("H"))
          ))
        )
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Ei saa olla numero opinto-ohjauksessa" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("OP1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("OP2")).copy(arviointi = numeerinenArviointi(8))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Opinto-ohjauksen moduulin moduulikoodistolops2021/OP1 arvosanan on oltava S tai H"),
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Opinto-ohjauksen moduulin moduulikoodistolops2021/OP2 arvosanan on oltava S tai H")
          )
        )
      }
    }

    "Ei saa olla kirjain muiden aineiden valtakunnallisissa moduuleissa" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE1", 2.0f)).copy(arviointi = sanallinenArviointi("S")),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE2", 2.0f)).copy(arviointi = sanallinenArviointi("H"))
        ))),
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KOLD1", 2.0f)).copy(arviointi = sanallinenArviointi("S")),
        ))),
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKB21", 2.0f, "ET")).copy(arviointi = sanallinenArviointi("S"))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Valtakunnallisen moduulin moduulikoodistolops2021/KE1 arvosanan on oltava numero"),
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Valtakunnallisen moduulin moduulikoodistolops2021/KE2 arvosanan on oltava numero"),
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Valtakunnallisen moduulin moduulikoodistolops2021/KOLD1 arvosanan on oltava numero"),
            exact(KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana, "Valtakunnallisen moduulin moduulikoodistolops2021/VKB21 arvosanan on oltava numero")
          )
        )
      }
    }

    "Saa olla kirjain paikallisissa opintojaksoissa" in {
      putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = sanallinenArviointi("S"))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }
  }

  "Kaksi samaa oppiainetta" - {
    "Lukion oppimäärän suorituksessa" - {
      "Identtisillä tiedoilla -> HTTP 400" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran"))
        }
      }
      "Eri kielivalinnalla -> HTTP 200" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI2", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Eri matematiikan oppimäärällä -> HTTP 400" in {
        putOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(vahvistamatonOppimääränSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)),
          oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAB")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9))
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus koskioppiaineetyleissivistava/MA esiintyy useammin kuin kerran"))
        }
      }
    }

    "Oppiaineiden oppimäärien suorituksessa" - {
      "Identtisillä tiedoilla -> HTTP 400" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran"))
        }
      }
      "Eri kielivalinnalla -> HTTP 200" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI2", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Eri matematiikan oppimäärällä -> HTTP 400" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)),
          oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAB")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9))
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus koskioppiaineetyleissivistava/MA esiintyy useammin kuin kerran"))
        }
      }
    }
  }

  "Vieraan kielen moduuli" - {
    "oppimäärän oppiaineissa" - {
      "vieraan kielen oppiaineessa oleville moduuleille lisätään tai korjataan kieli tiedonsiirrossa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA1", 2, Some("FI"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMA1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B3", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B2", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB21", 2, Some("SV"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B3", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "ES")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("B2", "PL")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2, Some("LV"))).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.flatMap(_.osasuoritusLista.map(_.koulutusmoduuli))

        val odotetutModuulit = List(
          vieraanKielenModuuliOppiaineissa("RUA1", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUB11", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUÄ1", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("FINA1", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FINB11", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FIM1", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("SMA1", 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("SMB31", 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("LAB21", 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("LAB31", 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("ENA1", 2, Some("EN")),
          vieraanKielenModuuliOppiaineissa("VKA1", 2, Some("ES")),
          vieraanKielenModuuliOppiaineissa("VKB21", 2, Some("PL"))
        )

        moduulit should equal(odotetutModuulit)
      }
    }

    "oppiaineiden suoritusten oppiaineissa" - {
      "vieraan kielen oppiaineessa oleville moduuleille lisätään tai korjataan kieli tiedonsiirrossa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA1", 2, Some("FI"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMA1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B3", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B2", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB21", 2, Some("SV"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("B3", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("A", "ES")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              oppiaineenSuoritus(lukionKieli2019("B2", "PL")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2, Some("LV"))).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.flatMap(_.osasuoritusLista.map(_.koulutusmoduuli))

        val odotetutModuulit = List(
          vieraanKielenModuuliOppiaineissa("RUA1", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUB11", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUÄ1", 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("FINA1", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FINB11", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FIM1", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("SMA1", 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("SMB31", 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("LAB21", 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("LAB31", 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("ENA1", 2, Some("EN")),
          vieraanKielenModuuliOppiaineissa("VKA1", 2, Some("ES")),
          vieraanKielenModuuliOppiaineissa("VKB21", 2, Some("PL"))
        )

        moduulit should equal(odotetutModuulit)
      }
    }

    "muissa suorituksissa" - {
      "muut kuin vieraan kielen moduulit säilyvät sellaisenaan" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KE1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("FI2")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.head.osasuoritusLista.map(_.koulutusmoduuli)

        val odotetutModuulit = List(
          muuModuuliMuissaOpinnoissa("KE1"),
          muuModuuliMuissaOpinnoissa("FI2")
        )

        moduulit should equal(odotetutModuulit)
      }

      "suomen, ruotsin, englannin, latinan ja saamen moduuleille lisätään tai korjataan kieli tiedonsiirrossa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("RUA1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("RUB11")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("RUÄ1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("FINA1", 2, "FI")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("FINB11")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("FIM1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("SMA1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("SMB31", 2, "FI")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("LAB21")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("LAB31")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("ENA1")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.head.osasuoritusLista.map(_.koulutusmoduuli)

        val odotetutModuulit = List(
          vieraanKielenModuuliMuissaOpinnoissa("RUA1", 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("RUB11", 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("RUÄ1", 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("FINA1", 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("FINB11", 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("FIM1", 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("SMA1", 2, "SE"),
          vieraanKielenModuuliMuissaOpinnoissa("SMB31", 2, "SE"),
          vieraanKielenModuuliMuissaOpinnoissa("LAB21", 2, "LA"),
          vieraanKielenModuuliMuissaOpinnoissa("LAB31", 2, "LA"),
          vieraanKielenModuuliMuissaOpinnoissa("ENA1", 2, "EN")
        )

        moduulit should equal(odotetutModuulit)
      }

      "Vieraan kielen VK-moduuleita ei voi siirtää ilman kieltä" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("VKA1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("VKAAB36")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Muissa suorituksissa olevalta vieraan kielen moduulilta moduulikoodistolops2021/VKA1 puuttuu kieli"),
              exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Muissa suorituksissa olevalta vieraan kielen moduulilta moduulikoodistolops2021/VKAAB36 puuttuu kieli")
            )
          )
        }
      }

      "muita kuin vieraan kielen moduuleita ei voi siirtää kielen kanssa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("ÄIS1", 2, "SE")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("PS2", 2, "FI")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Suoritukselle moduulikoodistolops2021/ÄIS1 on määritelty kieli, vaikka se ei ole vieraan kielen moduuli"),
              exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Suoritukselle moduulikoodistolops2021/PS2 on määritelty kieli, vaikka se ei ole vieraan kielen moduuli")
            )
          )
        }
      }
    }
  }

  "Vieraan kielen koodia kielivalikoima/97" - {
    "ei saa käyttää oppiaineessa" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(lukionKieli2019("A", "97")).copy(osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1")).copy(arviointi = numeerinenArviointi(10))
            )))
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi, "Suorituksessa koskioppiaineetyleissivistava/A käytettyä kielikoodia 97 ei sallita")
          )
        )
      }
    }

    "ei saa käyttää muissa suorituksissa olevan moduulin suorituksissa" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
              moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKA1", 2, "97")).copy(arviointi = numeerinenArviointi(10)),
              moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKAAB36", 2, "97")).copy(arviointi = numeerinenArviointi(10))
            )))
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi, "Suorituksessa moduulikoodistolops2021/VKA1 käytettyä kielikoodia 97 ei sallita"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi, "Suorituksessa moduulikoodistolops2021/VKAAB36 käytettyä kielikoodia 97 ei sallita")
          )
        )
      }
    }
  }

  "Äidinkielen kieltä oppiaineaidinkielijakirjallisuus/AIAI ei saa käyttää" in {
    val oo = aktiivinenOpiskeluoikeus.copy(
      suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
        osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AIAI", pakollinen = true)).copy(osasuoritukset = None)
        ))
      ))
    )

    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400,
        List(
          exact(
            KoskiErrorCategory.badRequest.validation.rakenne.deprekoituOppimäärä,
            "Suorituksessa koskioppiaineetyleissivistava/AI käytettyä kieltä AIAI ei sallita. Oman äidinkielen opinnot kuuluu siirtää vieraan kielen opintoina."
          )
        )
      )
    }
  }

  "Äidinkielen omainen oppiaine" - {
    def verify[A](kieli: String)(expect: => A): A = {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(Lukio2019ExampleData.lukionKieli2019("AOM", kieli)).copy(osasuoritukset = None)
          ))
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

  "Suullisen kielitaidon kokeen merkintä, jos sen sisältäviä moduuleita on suoritettu" - {

    "oppimäärän suorituksessa" - {
      "ei vaadita, jos ei ole vahvistettu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "HU")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("SMA8")).copy(arviointi = numeerinenArviointi(10)),
              )))
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }

      "ei vaadita, vaikka oppiaine olisi arvioitu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonOppimääränSuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              )
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }

      "vaaditaan, jos vahvistettu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppimääränSuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "HU")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("SMA8")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              valmistumiseenRiittäväMatematiikanSuoritus
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUB16 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUÄ8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FINA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FINB16 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FIM8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/ENA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/EN",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/VKA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/HU",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/SMA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SE",
              )
            )
          )
        }
      }

      "väärällä kielellä suoritettu koe ei kelpaa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppimääränSuoritusIlmanSuullisenKielitaidonKokeita.copy(
            suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoe("EN"))),
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              valmistumiseenRiittäväMatematiikanSuoritus
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              )
            )
          )
        }
      }
    }

    "oppiaineiden suorituksissa" - {
      "ei vaadita, jos ei ole arvioitu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "HU")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "SE")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }

      "vaaditaan, jos arvioitu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("B1", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("AOM", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "EN")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "HU")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              oppiaineenSuoritus(lukionKieli2019("A", "SE")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMA8")).copy(arviointi = numeerinenArviointi(10))))
              )
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUB16 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUÄ8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FINA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FINB16 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/FIM8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/FI",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/ENA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/EN",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/VKA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/HU",
              ),
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/SMA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SE",
              )
            )
          )
        }
      }

      "väärällä kielellä suoritettu koe ei kelpaa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
            suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoe("EN"))),
            osasuoritukset = Some(List(
              oppiaineenSuoritus(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              )
            ))
          ))
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400,
            List(
              exact(
                KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe,
                "Suoritus moduulikoodistolops2021/RUA8 vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä kielivalikoima/SV",
              )
            )
          )
        }
      }
    }
  }

  "Valtakunnalliset moduulit paikallisessa oppiaineessa" - {
    "Estetään yleisesti tietomallitasolla" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("FYM", "Leikkifysiikka"), "Leikkifysiikka")).copy(
              arviointi = numeerinenLukionOppiaineenArviointi(10),
              osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY1")).copy(arviointi = numeerinenArviointi(10))))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
      }
    }

    "Estetään validaatiolla, jos käytetty valtakunnallisen oppiaineen koodia" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("FY", "Leikkifysiikka"), "Leikkifysiikka")).copy(
              arviointi = numeerinenLukionOppiaineenArviointi(10),
              osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY1")).copy(arviointi = numeerinenArviointi(10))))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          List(
            exact(
              KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia,
              "Paikalliseen oppiaineeseen FY (Leikkifysiikka) ei voi lisätä valtakunnallista moduulia moduulikoodistolops2021/FY1. Paikallisessa oppiaineessa voi olla vain paikallisia opintojaksoja."
            )
          )
        )
      }
    }
  }

  "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
    val opiskeluoikeus = aktiivinenOpiskeluoikeus.copy(
      tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(LocalDate.of(2020, 5, 15), opiskeluoikeusEronnut)
      )),
      suoritukset = List(oppimääränSuoritus)
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
    }
  }


  private def oppimääränSuoritusIlmanSuullisenKielitaidonKokeita = oppimääränSuoritus.copy(suullisenKielitaidonKokeet = None)

  private def vahvistamatonOppimääränSuoritusIlmanSuullisenKielitaidonKokeita = vahvistamatonOppimääränSuoritus.copy(suullisenKielitaidonKokeet = None)

  private def valmistumiseenRiittäväMatematiikanSuoritus = oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(
    arviointi = numeerinenLukionOppiaineenArviointi(9),
    osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2", 150)).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3", 20).copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
    ))
  )

  private def suullisenKielitaidonKoe(kieli: String, arvosana: String = "6", taitotaso: String = "B1.1", kuvaus: Option[LocalizedString] = None) =
    SuullisenKielitaidonKoe2019(
      kieli = Koodistokoodiviite(kieli, "kielivalikoima"),
      arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoyleissivistava"),
      taitotaso = Koodistokoodiviite(taitotaso, koodistoUri = "arviointiasteikkokehittyvankielitaidontasot"),
      kuvaus = kuvaus,
      päivä = date(2019, 9, 3)
    )

  "Oman äidinkielen opintojen siirtäminen moduulina on estetty" in {
    val oo = aktiivinenOpiskeluoikeus.copy(
      suoritukset = List(vahvistamatonOppimääränSuoritus.copy(
        osasuoritukset = Some(List(
          oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OÄI1")),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("RÄI2")),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("SÄI3"))
          )))
        ))
      ))
    )

    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400,
        List(
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli OÄI1 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä."),
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli RÄI2 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä."),
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli SÄI3 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä.")
        )
      )
    }
  }

  "Suoritukset" - {
    "Useampi lukionoppimaara-suoritus aiheuttaa virheen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus, oppimääränSuoritus))) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
            """Opiskeluoikeudelle yritetään lukion oppimäärän lisäksi tallentaa useampi päätason suoritus. Lukion oppimäärän opiskelijalla voi olla vain yksi päätason suoritus."""
          )
        )
      }
    }

    "Ei voida olla samaan aikaan oppimäärän suorittaja ja aineopiskelija" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus,
        oppiaineidenOppimäärienSuoritus))) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
            """Opiskeluoikeudelle yritetään lukion oppimäärän lisäksi tallentaa useampi päätason suoritus. Lukion oppimäärän opiskelijalla voi olla vain yksi päätason suoritus."""
          )
        )
      }
    }
  }

  "oppimääräSuoritettu-kenttä" - {
    "Täytetään automaattisesti 'true'ksi kun oppimäärän suoritus vahvistettu" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus))
      val opiskeluoikeus: LukionOpiskeluoikeus = putAndGetOpiskeluoikeus(oo).asInstanceOf[LukionOpiskeluoikeus]

      opiskeluoikeus.oppimääräSuoritettu.get should equal(true)
    }

    "Ei täytetä automaattisesti 'true'ksi kun kyseessä oppiaineen oppimäärä" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
        vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 5, 15))
      )))
      val opiskeluoikeus: LukionOpiskeluoikeus = putAndGetOpiskeluoikeus(oo).asInstanceOf[LukionOpiskeluoikeus]

      opiskeluoikeus.oppimääräSuoritettu should equal(None)
    }

    "Ei täytetä automaattisesti, jos oppimäärän suoritusta ei ole vahvistettu" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )),
        suoritukset = List(oppimääränSuoritus.copy(
          vahvistus = None
        )))
      val opiskeluoikeus: LukionOpiskeluoikeus = putAndGetOpiskeluoikeus(oo).asInstanceOf[LukionOpiskeluoikeus]

      opiskeluoikeus.oppimääräSuoritettu.isDefined should equal(false)
    }

    "Jos nuorten oppimäärän suoritus sisältää alle 150 op, oppimäärää ei voi merkitä suoritetuksi" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(true),
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )),
        suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen),
          vahvistus = None
        )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
            s"Suoritus koulutus/309902 on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys"
          )
        )
      }
    }

    "Jos aikuisten oppimäärän suoritus sisältää alle 88 op, oppimäärää ei voi merkitä suoritetuksi" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(true),
        tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )),
        suoritukset = List(oppimääränSuoritus.copy(
          koulutusmoduuli = LukionOppimäärä(perusteenDiaarinumero = ExamplesLukio2019.lops2019AikuistenPerusteenDiaarinumero),
          oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma,
          osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen),
          vahvistus = None
        )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
            s"Suoritus koulutus/309902 on merkitty valmiiksi tai opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 88 op osasuorituksia, tai opiskeluoikeudelta puuttuu linkitys"
          )
        )
      }
    }

    "Jos nuorten oppiaineiden oppimäärän suoritus sisältää alle 150 op, oppimäärää ei voi merkitä suoritetuksi" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(true),
        suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
          osasuoritukset = Some(oppiainesuorituksetEiRiitäValmistumiseen),
          vahvistus = None
        )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia(
            s"Suorituksen lukionaineopinnot opiskeluoikeuden tiedoissa oppimäärä on merkitty suoritetuksi, mutta sillä ei ole 150 op osasuorituksia, joista vähintään 20 op valinnaisia, tai opiskeluoikeudelta puuttuu linkitys"
          )
        )
      }
    }

    "Jos nuorten oppiaineiden oppimäärän suoritus sisältää riittävästi opintopisteitä, oppimäärän voi merkitä suoritetuksi" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(true),
        suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
          osasuoritukset = Some(oppiainesuorituksetRiittääValmistumiseenNuorilla),
          vahvistus = None
        )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Oppiaineen oppimäärän suorituksen lukionOppimääräSuoritettu-kenttä deprekoitu, eikä sitä saa enää siirtää" in {
    val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
      lukionOppimääräSuoritettu = Some(true)
    )))
    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400,
        KoskiErrorCategory.badRequest.validation.rakenne.deprekoituLukionAineopintojenPäätasonSuorituksenKenttä()
      )
    }
  }

  "Historian kurssien konversiot kun perusteen diaarinumero 'OPH-2267-2019' (Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019)" - {
    "Lukion oppimäärä, perusteen diaarina 'OPH-2267-2019'" in {
      val oo = ExamplesLukio2019.aikuistenLukionOppimääräOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(false),
        suoritukset = List(aikuistenOppimäärienSuoritus.copy(
          osasuoritukset = Some(aikuistenOppiainesuoritusHistoria)
        ),
        ), tila = ExamplesLukio2019.aikuistenLukionOppimääräOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot = defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot.map(opiskeluoikeusjakso => opiskeluoikeusjakso.copy(tila = opiskeluoikeusAktiivinen))))
      val result = putAndGetOpiskeluoikeus(oo)
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should equal("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should equal("Kansainväliset suhteet")

      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("sv") should equal("Det självständiga Finlands historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("sv") should equal("Internationella relationer")
    }

    "Lukion oppiaineiden oppimäärä, perusteen diaarina 'OPH-2267-2019'" in {
      val oo = ExamplesLukio2019.aikuistenLukionOppiaineidenOppimääräOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(false),
        suoritukset = List(aikuistenOppiaineidenOppimäärienSuoritus.copy(
          osasuoritukset = Some(aikuistenOppiainesuoritusHistoria)
        ),
        ), tila = ExamplesLukio2019.aikuistenLukionOppimääräOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot = defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot.map(opiskeluoikeusjakso => opiskeluoikeusjakso.copy(tila = opiskeluoikeusAktiivinen))))
      val result = putAndGetOpiskeluoikeus(oo)
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should equal("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should equal("Kansainväliset suhteet")

      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("sv") should equal("Det självständiga Finlands historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("sv") should equal("Internationella relationer")
    }

    "Perusteen diaarinumerona jokin muu kuin 'OPH-2267-2019'; Konversiota ei tehdä" in {
      val oo = defaultOpiskeluoikeus.copy(
        oppimääräSuoritettu = Some(false),
        suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(
          osasuoritukset = Some(aikuistenOppiainesuoritusHistoria)
        ),
        ), tila = defaultOpiskeluoikeus.tila.copy(opiskeluoikeusjaksot = defaultOpiskeluoikeus.tila.opiskeluoikeusjaksot.map(opiskeluoikeusjakso => opiskeluoikeusjakso.copy(tila = opiskeluoikeusAktiivinen))))
      val result = putAndGetOpiskeluoikeus(oo)
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("fi") should not equal("Itsenäisen Suomen historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("fi") should not equal("Kansainväliset suhteet")

      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI2").get.koulutusmoduuli.nimi.get("sv") should not equal("Det självständiga Finlands historia")
      result.suoritukset.head.osasuoritusLista.head.osasuoritusLista.find(_.koulutusmoduuli.tunniste.koodiarvo == "HI3").get.koulutusmoduuli.nimi.get("sv") should not equal("Internationella relationer")
    }
  }

  private def putAndGetOpiskeluoikeus(oo: LukionOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: LukionOpiskeluoikeus = ExamplesLukio2019.opiskeluoikeus
}
