package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.{englanti, ruotsinKieli, suomenKieli}
import fi.oph.koski.documentation.ExamplesIB._
import fi.oph.koski.documentation.IBExampleData._
import fi.oph.koski.documentation.Lukio2019ExampleData.{laajuus, lukionKieli2019, muuModuuliMuissaOpinnoissa, muuModuuliOppiaineissa, numeerinenArviointi, numeerinenLukionOppiaineenArviointi, paikallinenOpintojakso, sanallinenArviointi, sanallinenLukionOppiaineenArviointi, vieraanKielenModuuliMuissaOpinnoissa, vieraanKielenModuuliOppiaineissa}
import fi.oph.koski.documentation.{ExamplesIB, Lukio2019ExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
import fi.oph.koski.http.ErrorMatcher.exact
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationPreIB2019Spec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[IBOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[IBOpiskeluoikeus]]

  "Vanhaa ja uutta Pre-IB suoritusta ei sallita samassa opiskeluoikeudessa" in {
    val oo = defaultOpiskeluoikeus.copy(suoritukset = List(
      ExamplesIB.preIBSuoritus2019,
      ExamplesIB.preIBSuoritus
    ))

    setupOppijaWithOpiskeluoikeus(oo) {
      verifyResponseStatus(400,
        List(
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia, "Vanhan ja lukion opetussuunnitelman 2019 mukaisia Pre-IB-opintoja ei sallita samassa opiskeluoikeudessa")
        ))
    }
  }

  "Laajuudet" - {
    "Oppiaineen laajuus" - {
      "Oppiaineen laajuus lasketaan moduuleiden ja paikallisten opintojaksojen laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(
            lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1").copy(laajuus = laajuus(2.5))).copy(arviointi = numeerinenArviointi(8)),
              paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT231", "Tanssin alkeet", "Rytmissä pysyminen").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7)),
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(laajuus = laajuus(0.5))).copy(arviointi = numeerinenArviointi(8))
            )))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Muiden opintojen laajuus lasketaan moduuleiden ja paikallisten opintojaksojen laajuuksista" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(
            muidenlukioOpintojenPreIBSuoritus2019(Lukio2019ExampleData.muutSuoritukset()).copy(osasuoritukset = Some(List(
              moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("ÄI1").copy(laajuus = laajuus(2.5))).copy(arviointi = numeerinenArviointi(8)),
              paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT231", "Tanssin alkeet", "Rytmissä pysyminen").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7)),
              moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("ÄI3").copy(laajuus = laajuus(0.5))).copy(arviointi = numeerinenArviointi(8))
            )))
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(4.5)
      }

      "Jos oppiaineella ei ole osasuorituksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(
            lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true).copy(laajuus = Some(laajuus(9.0)))).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Jos muilla suorituksilla ei ole osasuorituksia, sille asetettu laajuus poistetaan" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(
            muidenlukioOpintojenPreIBSuoritus2019(Lukio2019ExampleData.lukiodiplomit().copy(laajuus = Some(laajuus(9.0)))).copy(osasuoritukset = None)
          ))
        )))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
      }

      "Moduulin oletuslaajuus on 2" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3")).copy(arviointi = numeerinenArviointi(8))
          )))))
        )))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(6.0)
      }

      "Jos oppiaineella ei ole osasuorituksia laajuus on 0" in {
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(
          osasuoritukset = Some(List(lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(4)).copy(osasuoritukset = None)
        )))))

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(0)
      }
    }
  }

  "Merkintä erityisestä tutkinnosta" - {
    "oppiainetasolla estää moduulin ja paikallisen opintojakson lisäämisen kyseiseen oppiaineeseen" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.preIBSuoritus2019.copy(osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(suoritettuErityisenäTutkintona = true, arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10))
        ))),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
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
  }

  "Osasuoritustyypit" - {
    "Temaattisiin opintoihin ei voi siirtää moduuleita" in {
      setupOppijaWithOpiskeluoikeus(ExamplesIB.aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = Lukio2019ExampleData.sanallinenArviointi("S")),
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MAB2")).copy(arviointi = numeerinenArviointi(10)),
        )))
      )))))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
      }
    }

    "Kaikki lukiodiplomimoduulit voi siirtää lukiodiplomeihin" in {
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
          paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = Lukio2019ExampleData.sanallinenArviointi("S")),
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
          moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KOLD1", 1.0f)).copy(arviointi = numeerinenArviointi(5)),
        ))),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("KU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KULD2", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("LI")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LILD4", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("MU")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MULD6", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        ))),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("TE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
          moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("TELD8", 2.0f)).copy(arviointi = numeerinenArviointi(6)),
        )))
      )))))) {
        verifyResponseStatusOk()
      }
    }

    "Yleisesti lukiodiplomimoduuleita ei voi siirtää oppiaineisiin tai muihin suorituksiin" in {
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
          suorituskieli = Some(englanti),
          arviointi = numeerinenLukionOppiaineenArviointi(9),
          osasuoritukset = None),
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.matematiikka("MAA")).copy(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(
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
    "Saa olla S, laajuus on 2 tai alle" in {
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("LI")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LI1", 2.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
    }
    "Ei saa olla S, jos laajuus on yli 2 op" in {
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("KE")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("KE1", 3.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle("Oppiaineen koskioppiaineetyleissivistava/KE arvosanan pitää olla numero, jos oppiaineen laajuus on yli 2 op"))
      }
    }
    "Opinto-ohjauksessa" - {
      "Ei saa olla numeroarviointia" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = numeerinenLukionOppiaineenArviointi(5),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 2.0f)).copy(arviointi = sanallinenArviointi("S"))
            ))
          )
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana("Opinto-ohjauksen oppiaineen suorituksentyyppi/preiboppimaara2019 arvosanan on oltava S tai H"))
        }
      }
      "Saa olla sanallinen arviointi S, riippumatta laajuudesta" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 5.0f)).copy(arviointi = sanallinenArviointi("S"))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Saa olla sanallinen arviointi H, riippumatta laajuudesta" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("H"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1", 5.0f)).copy(arviointi = sanallinenArviointi("H"))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Liikunnassa" - {
      "Saa olla S, vaikka laajuus olisi yli 2 op" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("LI")).copy(
            arviointi = Some(List(
              SanallinenLukionOppiaineenArviointi2019("S"))
            ),
            osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("LI1", 2.0f)).copy(arviointi = numeerinenArviointi(5))
            ))
          )
        )))))) {
          verifyResponseStatusOk()
        }
      }

      "Viimeisin ei saa olla H, jos laajuus yli 2 op" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("LI")).copy(
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

    "Vieraassa kielessä" - {
      "Pakollisessa saa olla S, jos laajuus on 2 op tai alle" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = true)).copy(
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
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = true)).copy(
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
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
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
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
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
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suorituskieli = suomenKieli, osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("B2", "SV").copy(pakollinen = false)).copy(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("OP")).copy(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
        lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(6)).copy(osasuoritukset = Some(List(
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
      setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
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
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran"))
        }
      }
      "Eri kielivalinnalla -> HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None),
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI2", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = None)
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Eri matematiikan oppimäärällä -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)),
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.matematiikka("MAB")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9))
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
          suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
            osasuoritukset = Some(List(
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "SV")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA1", 2, Some("FI"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB11")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "FI")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMA1")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B3", "SE")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("SMB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B2", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB21", 2, Some("SV"))).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B3", "LA")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("LAB31")).copy(arviointi = numeerinenArviointi(10)),
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "EN")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "ES")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1")).copy(arviointi = numeerinenArviointi(10))
              ))),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B2", "PL")).copy(osasuoritukset = Some(List(
                moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKB21", 2, Some("LV"))).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.flatMap(_.osasuoritusLista.map(_.koulutusmoduuli))

        val odotetutModuulit = List(
          vieraanKielenModuuliOppiaineissa("RUA1"  , 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUB11" , 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("RUÄ1"  , 2, Some("SV")),
          vieraanKielenModuuliOppiaineissa("FINA1" , 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FINB11", 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("FIM1"  , 2, Some("FI")),
          vieraanKielenModuuliOppiaineissa("SMA1"  , 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("SMB31" , 2, Some("SE")),
          vieraanKielenModuuliOppiaineissa("LAB21" , 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("LAB31" , 2, Some("LA")),
          vieraanKielenModuuliOppiaineissa("ENA1"  , 2, Some("EN")),
          vieraanKielenModuuliOppiaineissa("VKA1"  , 2, Some("ES")),
          vieraanKielenModuuliOppiaineissa("VKB21" , 2, Some("PL"))
        )

        moduulit should equal(odotetutModuulit)
      }
    }

    "muissa suorituksissa" - {
      "muut kuin vieraan kielen moduulit säilyvät sellaisenaan" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KE1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("FI2")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.head.osasuoritusLista.map(_.koulutusmoduuli)

        val odotetutModuulit = List(
          muuModuuliMuissaOpinnoissa("KE1"),
          muuModuuliMuissaOpinnoissa("FI2")
        )

        moduulit should equal(odotetutModuulit)
      }

      "suomen, ruotsin, englannin, latinan ja saamen moduuleille lisätään tai korjataan kieli tiedonsiirrossa" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
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

        val opiskeluoikeus: Opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(oo)
        val moduulit = opiskeluoikeus.suoritukset.head.osasuoritusLista.head.osasuoritusLista.map(_.koulutusmoduuli)

        val odotetutModuulit = List(
          vieraanKielenModuuliMuissaOpinnoissa("RUA1"  , 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("RUB11" , 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("RUÄ1"  , 2, "SV"),
          vieraanKielenModuuliMuissaOpinnoissa("FINA1" , 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("FINB11", 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("FIM1"  , 2, "FI"),
          vieraanKielenModuuliMuissaOpinnoissa("SMA1"  , 2, "SE"),
          vieraanKielenModuuliMuissaOpinnoissa("SMB31" , 2, "SE"),
          vieraanKielenModuuliMuissaOpinnoissa("LAB21" , 2, "LA"),
          vieraanKielenModuuliMuissaOpinnoissa("LAB31" , 2, "LA"),
          vieraanKielenModuuliMuissaOpinnoissa("ENA1"  , 2, "EN")
        )

        moduulit should equal(odotetutModuulit)
      }

      "Vieraan kielen VK-moduuleita ei voi siirtää ilman kieltä" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("VKA1")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("VKAAB36")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
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
          suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
            osasuoritukset = Some(List(
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("ÄIS1", 2, "SE")).copy(arviointi = numeerinenArviointi(10)),
                moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("PS2", 2, "FI")).copy(arviointi = numeerinenArviointi(10))
              )))
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
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
        suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
          osasuoritukset = Some(List(
            lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "97")).copy(osasuoritukset = Some(List(
              moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1")).copy(arviointi = numeerinenArviointi(10))
            )))
          ))
        ))
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400,
          List(
            exact(KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi, "Suorituksessa koskioppiaineetyleissivistava/A käytettyä kielikoodia 97 ei sallita")
          )
        )
      }
    }

    "ei saa käyttää muissa suorituksissa olevan moduulin suorituksissa" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
          osasuoritukset = Some(List(
            muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
              moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKA1", 2, "97")).copy(arviointi = numeerinenArviointi(10)),
              moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKAAB36", 2, "97")).copy(arviointi = numeerinenArviointi(10))
            )))
          ))
        ))
      )

      setupOppijaWithOpiskeluoikeus(oo) {
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
      suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
        osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AIAI", pakollinen = true)).copy(osasuoritukset = None)
        ))
      ))
    )

    setupOppijaWithOpiskeluoikeus(oo) {
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

  "Suullisen kielitaidon kokeen merkintä, jos sen sisältäviä moduuleita on suoritettu" - {

    "oppimäärän suorituksessa" - {
      "ei vaadita, jos ei ole vahvistettu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonPreIB2019SuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "SV")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "FI")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "EN")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "HU")).copy(
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("SMA8")).copy(arviointi = numeerinenArviointi(10)),
              )))
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }

      "ei vaadita, vaikka oppiaine olisi arvioitu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(vahvistamatonPreIB2019SuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              )
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatusOk()
        }
      }

      "vaaditaan, jos vahvistettu" in {
        val oo = aktiivinenOpiskeluoikeus.copy(
          suoritukset = List(preIB2019SuoritusIlmanSuullisenKielitaidonKokeita.copy(
            osasuoritukset = Some(List(
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUÄ8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("B1", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FINB16")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("AOM", "FI")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("FIM8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "EN")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("ENA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "HU")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8")).copy(arviointi = numeerinenArviointi(10))))
              ),
              muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
                moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("SMA8")).copy(arviointi = numeerinenArviointi(10)),
              )))
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
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
          suoritukset = List(preIB2019SuoritusIlmanSuullisenKielitaidonKokeita.copy(
            suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoe("EN"))),
            osasuoritukset = Some(List(
              lukionOppiaineenPreIBSuoritus2019(lukionKieli2019("A", "SV")).copy(
                arviointi = numeerinenLukionOppiaineenArviointi(10),
                osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA8")).copy(arviointi = numeerinenArviointi(10))))
              )
            ))
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
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
        suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
          osasuoritukset = Some(List(
            lukionOppiaineenPreIBSuoritus2019(PaikallinenLukionOppiaine2019(PaikallinenKoodi("FYM", "Leikkifysiikka"), "Leikkifysiikka")).copy(
              arviointi = numeerinenLukionOppiaineenArviointi(10),
              osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY1")).copy(arviointi = numeerinenArviointi(10))))
            )
          ))
        ))
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*".r))
      }
    }

    "Estetään validaatiolla, jos käytetty valtakunnallisen oppiaineen koodia" in {
      val oo = aktiivinenOpiskeluoikeus.copy(
        suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
          osasuoritukset = Some(List(
            lukionOppiaineenPreIBSuoritus2019(PaikallinenLukionOppiaine2019(PaikallinenKoodi("FY", "Leikkifysiikka"), "Leikkifysiikka")).copy(
              arviointi = numeerinenLukionOppiaineenArviointi(10),
              osasuoritukset = Some(List(moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY1")).copy(arviointi = numeerinenArviointi(10))))
            )
          ))
        ))
      )

      setupOppijaWithOpiskeluoikeus(oo) {
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

  "Maksuttomuus" - {
    "Sallitaan maksuttomuustiedot PreIB2019-suorituksissa" in {
        setupOppijaWithOpiskeluoikeus(aktiivinenOpiskeluoikeus.copy(
            lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(maksuttomuus = Some(List(Maksuttomuus(alku = date(2021, 9, 1) , loppu = None, maksuton = true))))),
            suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus)),
          henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatusOk()
        }
    }
  }

  private def preIB2019SuoritusIlmanSuullisenKielitaidonKokeita = ExamplesIB.preIBSuoritus2019.copy(suullisenKielitaidonKokeet = None)

  private def vahvistamatonPreIB2019SuoritusIlmanSuullisenKielitaidonKokeita = ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(suullisenKielitaidonKokeet = None)

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
      suoritukset = List(ExamplesIB.vahvistamatonPreIB2019Suoritus.copy(
        osasuoritukset = Some(List(
          lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", pakollinen = true)).copy(osasuoritukset = Some(List(
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OÄI1")),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("RÄI2")),
            moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("SÄI3"))
          )))
        ))
      ))
    )

    setupOppijaWithOpiskeluoikeus(oo) {
      verifyResponseStatus(400,
        List(
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli OÄI1 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä."),
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli RÄI2 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä."),
          exact(KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia, "Moduuli SÄI3 ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä.")
        )
      )
    }
  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: IBOpiskeluoikeus): Opiskeluoikeus = setupOppijaWithOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: IBOpiskeluoikeus = ExamplesIB.opiskeluoikeusPreIB2019
}
