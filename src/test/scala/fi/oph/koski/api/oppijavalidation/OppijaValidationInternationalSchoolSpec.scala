package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.InternationalSchoolExampleData.{diplomaArviointi, diplomaIBOppiaine, diplomaKieliOppiaine, diplomaOppiaineenSuoritus, internationalSchoolOfHelsinki}
import fi.oph.koski.documentation.{ExampleData, InternationalSchoolExampleData, LukioExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationInternationalSchoolSpec extends AnyFreeSpec with KoskiHttpSpec with PutOpiskeluoikeusTestMethods[InternationalSchoolOpiskeluoikeus] {
  "Kaksi äidinkieltä" - {
    "Samalla kielivalinnalla -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6)),
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6))
        ))
      )))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (oppiaineetib/A,kielivalikoima/FI) esiintyy useammin kuin kerran"))
      }
    }

    "Eri kielivalinnalla -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6)),
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "EN"), diplomaArviointi(6))
        ))
      )))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Uudella oppiaineella DIS -> HTTP 200" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diplomaOppiaineenSuoritus(diplomaIBOppiaine("DIS"), diplomaArviointi(6))
        ))
      )))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Päätason suorituksen alkamispäivä" - {
    "Vaaditaan diploma luokka-asteilta" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(
        tutkintoSuoritus.copy(
          alkamispäivä = None
        )
      ))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu("Suoritukselle internationalschoolluokkaaste/12 ei ole merkitty alkamispäivää"))
      }
    }

    "Vaaditaan myp luokka-asteelta 10" in {
      val grade = 10

      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(
        MYPVuosiluokanSuoritus(
          koulutusmoduuli = MYPLuokkaAste(tunniste = Koodistokoodiviite(grade.toString, "internationalschoolluokkaaste")),
          luokka = Some(s"${grade.toString}B"),
          alkamispäivä = None,
          toimipiste = internationalSchoolOfHelsinki,
          vahvistus = None,
          suorituskieli = ExampleData.englanti
        )
      ))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu("Suoritukselle internationalschoolluokkaaste/10 ei ole merkitty alkamispäivää"))
      }
    }

    "Ei vaadita muilta myp luokka-asteilta" in {
      val grade = 9

      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(
        MYPVuosiluokanSuoritus(
          koulutusmoduuli = MYPLuokkaAste(tunniste = Koodistokoodiviite(grade.toString, "internationalschoolluokkaaste")),
          luokka = Some(s"${grade.toString}B"),
          alkamispäivä = None,
          toimipiste = internationalSchoolOfHelsinki,
          vahvistus = None,
          suorituskieli = ExampleData.englanti
        )
      ))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Ei vaadita pyp luokka-asteilta" in {
      val grade = 4

      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(
        PYPVuosiluokanSuoritus(
          koulutusmoduuli = PYPLuokkaAste(tunniste = Koodistokoodiviite(grade.toString, "internationalschoolluokkaaste")),
          luokka = Some(s"${grade.toString}B"),
          alkamispäivä = None,
          toimipiste = internationalSchoolOfHelsinki,
          vahvistus = None,
          suorituskieli = ExampleData.englanti
        )
      ))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  override def tag = implicitly[reflect.runtime.universe.TypeTag[InternationalSchoolOpiskeluoikeus]]
  def tutkintoSuoritus: DiplomaVuosiluokanSuoritus = InternationalSchoolExampleData.diplomaSuoritus(12, date(2019, 8, 15), None)

  override def defaultOpiskeluoikeus = InternationalSchoolOpiskeluoikeus(
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(tutkintoSuoritus)
  )
}
