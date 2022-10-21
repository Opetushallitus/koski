package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.EuropeanSchoolOfHelsinkiExampleData.{primarySuoritus, secondaryLowerSuoritus, secondaryNumericalMarkArviointi, secondaryS7FinalMarkArviointi, secondaryS7PreliminaryMarkArviointi, secondaryUpperMuunOppiaineenOsasuoritus, secondaryUpperSuoritus}
import fi.oph.koski.documentation.ExamplesEuropeanSchoolOfHelsinki.alkamispäivä
import fi.oph.koski.documentation.{ExampleData, ExamplesEuropeanSchoolOfHelsinki}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationEuropeanSchoolOfHelsinkiSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[EuropeanSchoolOfHelsinkiOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
      verifyResponseStatusOk()
    }
  }

  "Synteettisiä arvosanoja käyttävissä arvioinneissa" - {
    val alkamispäivä = ExamplesEuropeanSchoolOfHelsinki.alkamispäivä
    val arviointipäivä = alkamispäivä.plusMonths(1)
    "yli 10 arvosanoja" - {
      "ei hyväksytä NumericalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S6", "10.5", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7PreliminaryMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "10.1", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7FinalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "10.10", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
    }

    "10 desimaaleilla" - {
      "ei hyväksytä NumericalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S6", "10.0", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7PreliminaryMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "10.0", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7FinalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "10.00", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
    }

    "0 desimaaleilla" - {
      "ei hyväksytä NumericalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S6", "0.0", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7PreliminaryMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "0.0", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "Hyväksytään S7FinalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "0.00", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatusOk()
        }
      }
    }

    "alle 0 arvosanoja ei hyväksytä" - {
      "ei hyväksytä NumericalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S6", "-0.5", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7PreliminaryMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "-0.1", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
      "ei hyväksytä S7FinalMark:ssa" in {
        putOpiskeluoikeus(teeOpiskeluoikeus("S7", "-0.01", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
          verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
        }
      }
    }

    "desimaalien määrä" - {
      "NumericalMark:ssa" - {
        "hyväksytään .0" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S6", "4.0", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }
        "hyväksytään .5" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S6", "4.5", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }

        }
        "ei hyväksytä muita" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S6", "4.4", secondaryNumericalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
          }
        }
      }

      "S7PreliminaryMark:ssa" - {
        "hyväksytään .0" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "6.0", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }
        "hyväksytään 1 desimaali" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "6.3", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }
        "ei hyväksytä yli 1 desimaalia" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "6.38", secondaryS7PreliminaryMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
          }
        }
      }

      "S7FinalMark:ssa" - {
        "hyväksytään .00" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "9.00", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }

        "hyväksytään 2 desimaalia" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "9.78", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatusOk()
          }
        }

        "ei hyväksytä vain 1 desimaalia" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "8.9", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
          }
        }

        "ei hyväksytä yli 2 desimaalia" in {
          putOpiskeluoikeus(teeOpiskeluoikeus("S7", "8.988", secondaryS7FinalMarkArviointi), henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
            verifyResponseStatus(400, ErrorMatcher.regex( KoskiErrorCategory.badRequest.validation.jsonSchema, ".*ei ole kelvollinen synteettinen koodiarvo koodistossa.*".r))
          }
        }

      }
    }

    def teeOpiskeluoikeus(luokkaAste: String, arvosana: String, teeArviointi: (String, Option[LocalizedString], Option[List[Arvioitsija]], LocalDate) => Option[List[SecondaryArviointi]]) = {
      defaultOpiskeluoikeus.copy(
        suoritukset = List(
          secondaryUpperSuoritus(luokkaAste, alkamispäivä.plusYears(1)).copy(
            osasuoritukset = Some(List(
              secondaryUpperMuunOppiaineenOsasuoritus(
                oppiainekoodi = "PE",
                arviointi = teeArviointi(
                  arvosana,
                  None,
                  Some(List(Arvioitsija("Pekka Paunanen"))),
                  arviointipäivä
                )
              ),
            ))
          )
        )
      )
    }
  }

  "Koulutustyyppi" - {
    "Täydennetään" in {
      val putOo = defaultOpiskeluoikeus.copy(
        suoritukset = List(
          primarySuoritus("P1", alkamispäivä.plusYears(2)).copy(
            koulutusmoduuli = PrimaryLuokkaAste("P1").copy(koulutustyyppi = None)
          ),
          secondaryLowerSuoritus("S1", alkamispäivä.plusYears(8)).copy(
            koulutusmoduuli = SecondaryLowerLuokkaAste("S1").copy(koulutustyyppi = None)
          ),
          secondaryUpperSuoritus("S6", alkamispäivä.plusYears(13)).copy(
            koulutusmoduuli = SecondaryUpperLuokkaAste("S6").copy(koulutustyyppi = None)
          ),
        )
      )

      koulutustyypit(putOo) should be(List.empty)

      val oo = putAndGetOpiskeluoikeus(putOo)

      koulutustyypit(oo) should be(List("21", "21", "21"))
    }

    def koulutustyypit(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): List[String] = {
      oo.suoritukset.flatMap(_.koulutusmoduuli match {
        case k: KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste =>
          k.koulutustyyppi
        case _ => None
      }).map(_.koodiarvo)
    }
  }

  "Opintojen rahoitus" - {

    val alkamispäivä = defaultOpiskeluoikeus.alkamispäivä.get
    val päättymispäivä = alkamispäivä.plusYears(20)

    "lasna -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, opintojenRahoitus = None)))))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu))
    }

    "valmistunut -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusValmistunut, opintojenRahoitus = None)
      ))

      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu, ExampleData.muutaKauttaRahoitettu))
    }

    "Opintojen rahoitus on kielletty muilta tiloilta" in {
      def verifyRahoitusmuotoKielletty(tila: Koodistokoodiviite) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, tila, Some(ExampleData.muutaKauttaRahoitettu))
        )))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilallaEiSaaOllaRahoitusmuotoa(s"Opiskeluoikeuden tilalla ${tila.koodiarvo} ei saa olla rahoitusmuotoa"))
        }
      }

      List(
        ExampleData.opiskeluoikeusEronnut,
        ExampleData.opiskeluoikeusValiaikaisestiKeskeytynyt,
      ).foreach(verifyRahoitusmuotoKielletty)
    }
  }

  // TODO: TOR-1685 Lisää tarvittavat testit validaatioita toteutettaessa

  private def putAndGetOpiskeluoikeus(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): EuropeanSchoolOfHelsinkiOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
}
