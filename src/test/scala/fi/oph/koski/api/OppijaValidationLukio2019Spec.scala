package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{englanti, ruotsinKieli, suomenKieli, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.ExamplesLukio2019.{oppiaineidenOppimäärienSuoritus, oppimääränSuoritus}
import fi.oph.koski.documentation.Lukio2019ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{arviointi, numeerinenArviointi, sanallinenArviointi}
import fi.oph.koski.documentation.{ExamplesLukio2019, Lukio2019ExampleData, LukioExampleData}
import fi.oph.koski.http.ErrorMatcher.exact
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import java.time.LocalDate.{of => date}

class OppijaValidationLukio2019Spec extends TutkinnonPerusteetTest[LukionOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  "Laajuudet" - {
    "Oppiaineen laajuus" - {
      "Oppiaineen laajuus lasketaan moduleiden laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(
            oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = LukioExampleData.arviointi("9")).copy(osasuoritukset = Some(List(
              moduulinSuoritus(moduuli("OÄI1").copy(laajuus = laajuus(2.5))).copy(arviointi = numeerinenArviointi(8)),
              moduulinSuoritus(moduuli("OÄI2").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(8)),
              moduulinSuoritus(moduuli("OÄI3").copy(laajuus = laajuus(0.5))).copy(arviointi = numeerinenArviointi(8))
            )))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Modulin oletuslaajuus on 2" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = LukioExampleData.arviointi("9")).copy(osasuoritukset = Some(List(
            moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritus(moduuli("OÄI3")).copy(arviointi = numeerinenArviointi(8))
          )))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(6.0)
      }

      "Jos oppiaineella ei ole osasuorituksia laajuus on 0" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(
          osasuoritukset = Some(List(oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = LukioExampleData.arviointi("4")).copy(osasuoritukset = None)
        )))))

        val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(0)
      }
    }
  }

  "Diaarinumerot" - {
    "Nuorten ops" - {
      "Vain peruste OPH-2263-2019 sallitaan" in {
        val oppimääräAikuistenPerusteella = oppimääränSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = Some("OPH-2267-2019"))
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(koulutusmoduuli = oppimääräAikuistenPerusteella)))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2267-2019" suorituksella lukionoppimaara2019, sallitut arvot: OPH-2263-2019"""))
        }
      }
    }

    "Aikuisten ops" - {
      "Vain peruste OPH-2267-2019 sallitaan" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(oppimäärä = LukioExampleData.aikuistenOpetussuunnitelma)))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.vääräDiaari("""Väärä diaarinumero "OPH-2263-2019" suorituksella lukionoppimaara2019, sallitut arvot: OPH-2267-2019"""))
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
          moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = numeerinenArviointi(8)),
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
          moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = numeerinenArviointi(8)),
        )))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("""Lukion oppiaineiden oppimäärien suorituksen 2019 sisältävää opiskeluoikeutta ei voi merkitä valmiiksi ilman arvioitua oppiaineen osasuoritusta"""))
      }
    }
  }

  "Merkintä erityisestä tutkinnosta" - {
    "oppiainetasolla sallii oppiaineen ilman osasuorituksia" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(suoritettuErityisenäTutkintona = true, arviointi = arviointi("9")).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB3")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB4")).copy(arviointi = sanallinenArviointi("O"))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "oppiainetasolla estää moduulin ja paikallisen opintojakson lisäämisen kyseiseen oppiaineeseen" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(suoritettuErityisenäTutkintona = true, arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB3")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB4")).copy(arviointi = sanallinenArviointi("O"))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI1 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI2 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI3 ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus FY123 (Keittiöfysiikka) ei ole sallittu, koska oppiaine on suoritettu erityisenä tutkintona")
          ))
      }
    }

    "suoritustasolla sallii oppiaineet ilman osasuorituksia" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = None))))))) {
        verifyResponseStatusOk()
      }
    }

    "suoritustasolla sallii osasuoritukset muissa lukio-opinnoissa, temaattisissa opinnoissa tai lukiodiplomeissa" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = None),
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KE3")).copy(arviointi = numeerinenArviointi(10)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("HAI765", "Kansanmusiikki haitarilla", "Kansamusiikkia 2-rivisellä haitarilla")).copy(arviointi = sanallinenArviointi("S"))
        ))),
        temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S"))
        ))),
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritus(moduuli("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(9))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "suoritustasolla estää osasuoritusten lisäämisen oppiaineisiin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true,osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("OÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB3")).copy(arviointi = sanallinenArviointi("H")),
          moduulinSuoritus(moduuli("MAB4")).copy(arviointi = sanallinenArviointi("O")),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10)),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI1 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI2 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.erityisenäTutkintonaSuoritettuSisältääOsasuorituksia, "Osasuoritus moduulikoodistolops2021/OÄI3 ei ole sallittu, koska tutkinto on suoritettu erityisenä tutkintona"),
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
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S")),
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
        )))
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia("""Valtakunnallista moduulia moduulikoodistolops2021/MAB2 ei voi tallentaa temaattisiin opintoihin"""))
      }
    }

    "Kaikki lukiodiplomimoduulit voi siirtää lukiodiplomeihin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritus(moduuli("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritus(moduuli("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritus(moduuli("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritus(moduuli("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Lukiodiplomeihin ei voi siirtää paikallisia opintojaksoja tai muita kuin lukiodiplomimoduuleita" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suoritettuErityisenäTutkintona = true, osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S")),
          moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuoritus KAN200 (Kanteleensoiton perusteet) ei ole sallittu lukiodiplomi"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Osasuoritus moduulikoodistolops2021/MAB2 ei ole sallittu lukiodiplomi")
          ))
      }
    }

    "Lukiodiplomimoduulien laajuus ei voi olla muuta kuin 2 opintopistettä" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KOLD1", 1.0f)).copy(arviointi = numeerinenArviointi(5)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KULD2", 2.5f)).copy(arviointi = numeerinenArviointi(6)),
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä, "Osasuorituksen moduulikoodistolops2021/KOLD1 laajuus ei ole lukiodiplomille ainoa sallittu 2 opintopistettä"),
            exact(KoskiErrorCategory.badRequest.validation.laajuudet.lukiodiplominLaajuusEiOle2Opintopistettä, "Osasuorituksen moduulikoodistolops2021/KULD2 laajuus ei ole lukiodiplomille ainoa sallittu 2 opintopistettä")
          ))
      }
    }

    "Oppiainespesifit lukiodiplomit voi siirtää kyseisiin oppiaineisiin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = arviointi("6")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("LI")).copy(arviointi = arviointi("6")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("MU")).copy(arviointi = arviointi("6")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("TE")).copy(arviointi = arviointi("6")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Yleisesti lukiodiplomimoduuleita ei voi siirtää oppiaineisiin tai muihin suorituksiin" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = arviointi("6")).copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritus(moduuli("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritus(moduuli("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritus(moduuli("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritus(moduuli("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        ))),
        muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritus(moduuli("KOLD1", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
          moduulinSuoritus(moduuli("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          moduulinSuoritus(moduuli("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(9)),
          moduulinSuoritus(moduuli("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(10)),
          moduulinSuoritus(moduuli("TALD7", 2.0f)).copy(arviointi = numeerinenArviointi(5)),
          moduulinSuoritus(moduuli("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6))
        )))
      )))))) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KOLD1 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KULD2 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KÄLD3 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/LILD4 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/MELD5 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/MULD6 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/TALD7 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/TELD8 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),

            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KOLD1 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KULD2 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/KÄLD3 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/LILD4 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/MELD5 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/MULD6 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/TALD7 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
            exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Lukiodiplomi moduulikoodistolops2021/TELD8 ei ole sallittu oppiaineen tai muiden lukio-opintojen osasuoritus"),
          )
        )
      }
    }
  }

  "Suorituskieli" - {
    "Ei saa olla oppiainetasolla sama kuin päätason suorituksessa" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
          suorituskieli = Some(englanti),
          arviointi = arviointi("9"),
          osasuoritukset = None),
        oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(
          suorituskieli = Some(suomenKieli),
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            moduulinSuoritus(moduuli("MAB2")).copy(arviointi = sanallinenArviointi("H")),
            moduulinSuoritus(moduuli("MAB3")).copy(arviointi = sanallinenArviointi("H")),
            moduulinSuoritus(moduuli("MAB4")).copy(arviointi = sanallinenArviointi("O"))
          ))
        )
      )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia("""Oppiaineen koskioppiaineetyleissivistava/MA suorituskieli ei saa olla sama kuin päätason suorituksen suorituskieli"""))
      }
    }

    "Ei saa olla moduulitasolla sama kuin oppiaineessa tai päätason suorituksessa" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
          suorituskieli = Some(englanti),
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            moduulinSuoritus(moduuli("ÄI1")).copy(suorituskieli = Some(ruotsinKieli), arviointi = sanallinenArviointi("H")),
            moduulinSuoritus(moduuli("ÄI2")).copy(suorituskieli = Some(suomenKieli), arviointi = sanallinenArviointi("H")),
            moduulinSuoritus(moduuli("ÄI3")).copy(suorituskieli = Some(englanti), arviointi = sanallinenArviointi("O")),
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

  private def putAndGetOpiskeluoikeus(oo: LukionOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: LukionOpiskeluoikeus = ExamplesLukio2019.opiskeluoikeus
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): LukionOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(suoritukset = List(oppimääränSuoritus.copy(koulutusmoduuli = oppimääränSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))))

  // Lukio 2019 rajoittaa sallitut diaarinumerot arvoihin OPH-2263-2019 ja OPH-2267-2019 -> pakko käyttää tässä eperusteista löytyvää
  override def eperusteistaLöytymätönValidiDiaarinumero: String = "OPH-2263-2019"
}
