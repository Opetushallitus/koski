package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus.{aikuistenPerusopetuksenAlkuvaiheenSuoritus, oppiaineidenSuoritukset2015, oppiaineidenSuoritukset2017}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.http._
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

  private def opiskeluoikeusWithValmistunutTila = defaultOpiskeluoikeus.copy(
    tila = AikuistenPerusopetuksenOpiskeluoikeudenTila(List(
      AikuistenPerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä),
      AikuistenPerusopetuksenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusValmistunut)
    ))
  )

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

  "Vahvistetussa alkuvaiheen suorituksessa" - {
    "oppiaineen arviointia" - {
      "ei vaadita" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(_.map(_.copy(arviointi = None)))
          ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatusOk()
        }
      }
    }
    "kurssin arviointi" - {
      "vaaditaan" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(
            osasuoritukset = aikuistenPerusopetuksenAlkuvaiheenSuoritus.osasuoritukset.map(xs =>
              List(xs.head.copy(
                arviointi = None,
                osasuoritukset = xs.head.osasuoritukset.map(x => List(x.head.copy(arviointi = None))))
              ))
            ))
        )
        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, HttpStatus.append(
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella suorituksentyyppi/aikuistenperusopetuksenoppimaaranalkuvaihe on keskeneräinen osasuoritus aikuistenperusopetuksenalkuvaiheenkurssit2017/LÄI1"),
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella aikuistenperusopetuksenalkuvaiheenoppiaineet/AI on keskeneräinen osasuoritus aikuistenperusopetuksenalkuvaiheenkurssit2017/LÄI1"))
          )
        }
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

  "Opiskeluoikeuden Valmistunut tila" - {
    "Voidaan asettaa vaikka alkuvaiheen suorituksella ei olisi vahvistusta" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(
          aikuistenPerusopetuksenOppimääränSuoritus(),
          aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(vahvistus = None)
        )
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
    "Voidaan asettaa kun vahvistettu alkuvaihe ainoana suorituksena" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus))
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
    "Ei voida asettaa kun vahvistamaton oppimäärä on ainut suoritus" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(aikuistenPerusopetuksenOppimääränSuoritus().copy(vahvistus = None))
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta koulutus/201101 puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
      }
    }
    "Ei voida asettaa kun vahvistamaton alkuvaihe on ainut suoritus" in {
      val opiskeluoikeus = opiskeluoikeusWithValmistunutTila.copy(
        suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus.copy(vahvistus = None))
      )
      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta suorituksentyyppi/aikuistenperusopetuksenoppimaaranalkuvaihe puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Valmistunut"))
      }
    }
  }
}
