package fi.oph.koski.api

import java.time.LocalDate.{of => date}

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
