package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.InternationalSchoolExampleData.{diplomaArviointi, diplomaKieliOppiaine, diplomaOppiaineenSuoritus}
import fi.oph.koski.documentation.{InternationalSchoolExampleData, LukioExampleData}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

class OppijaValidationInternationalSchoolSpec extends FreeSpec with LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[InternationalSchoolOpiskeluoikeus] {
  "Kaksi äidinkieltä" - {
    "Samalla kielivalinnalla -> HTTP 400" in {
      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(
        osasuoritukset = Some(List(
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6)),
          diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6))
        ))
      )))

      putOpiskeluoikeus(oo) {
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

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Opintojen rahoitus" - {
    "lasna -tilalta vaaditaan opintojen rahoitus" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(date(2008, 1, 1), opiskeluoikeusLäsnä))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta lasna puuttuu opintojen rahoitus"))
      }
    }
    "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
      val tila = LukionOpiskeluoikeudenTila(List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2019, 8, 15), opiskeluoikeusValmistunut)
      ))
      val suoritus =  InternationalSchoolExampleData.diplomaSuoritus(12, date(2012, 12, 12), Some(date(2019, 8, 15))).copy(osasuoritukset = Some(List(
        diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI"), diplomaArviointi(6)),
        diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "EN"), diplomaArviointi(6))
      )))

      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila, suoritukset = List(suoritus))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta valmistunut puuttuu opintojen rahoitus"))
      }
    }
  }

  override def tag = implicitly[reflect.runtime.universe.TypeTag[InternationalSchoolOpiskeluoikeus]]
  def tutkintoSuoritus: DiplomaVuosiluokanSuoritus = InternationalSchoolExampleData.diplomaSuoritus(12, date(2019, 8, 15), None)

  override def defaultOpiskeluoikeus = InternationalSchoolOpiskeluoikeus(
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(valtionosuusRahoitteinen))
      )
    ),
    suoritukset = List(tutkintoSuoritus)
  )
}
