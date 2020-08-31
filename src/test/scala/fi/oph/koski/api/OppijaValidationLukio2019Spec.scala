package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesLukio2019.oppimääränSuoritus
import fi.oph.koski.documentation.Lukio2019ExampleData._
import fi.oph.koski.documentation.LukioExampleData.numeerinenArviointi
import fi.oph.koski.documentation.{ExamplesLukio2019, Lukio2019ExampleData, LukioExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

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
