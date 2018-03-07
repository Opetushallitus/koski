package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetuksenAlkuvaiheenSuoritus, oppiaineidenSuoritukset2015, oppiaineidenSuoritukset2017}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._

class OppijaValidationAikuistenPerusopetusSpec extends TutkinnonPerusteetTest[AikuistenPerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAikuistenPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = AikuistenPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      aikuistenPerusopetuksenOppimääränSuoritus(diaari).copy(osasuoritukset = None, vahvistus = None)
    ),
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  private def aikuistenPerusopetuksenOppimääränSuoritus(diaari: Option[String] = Some("19/011/2015")) = {
    ExamplesAikuistenPerusopetus.aikuistenPerusopetukseOppimääränSuoritus(
      AikuistenPerusopetus(diaari),
      (if (diaari == Some("OPH-1280-2017")) { oppiaineidenSuoritukset2017 } else { oppiaineidenSuoritukset2015 })
    )
  }

  def eperusteistaLöytymätönValidiDiaarinumero: String = "19/011/2015"

  "Kurssisuoritukset" - {
    "OPS 2015, mutta kurssisuorituksissa 2017 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("19/011/2015")).copy(osasuoritukset = oppiaineidenSuoritukset2017)))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*aikuistenperusopetuksenpaattovaiheenkurssit2017.*".r))
      }
    }

    "OPS 2017, mutta kurssisuorituksissa 2015 koodisto -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus(Some("OPH-1280-2017")).copy(osasuoritukset = oppiaineidenSuoritukset2015)))) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*aikuistenperusopetuksenkurssit2015.*".r))
      }
    }
  }

  "Sama oppiaine" - {
    "aikuisten perusopetuksen oppivaiheessa" - {
      "sallitaan" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(xs => xs.head :: xs)
          ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }
    "oppimäärän suorituksessa" - {
      "ei sallita" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus().copy(
            osasuoritukset = oppiaineidenSuoritukset2015.map(xs => xs.head :: xs))
          ))
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran ryhmässä pakolliset"))
        }
      }
    }
  }
}
