package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesIB._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema.{IBOpiskeluoikeus, IBOppiaineenSuoritus, LukionOpiskeluoikeudenTila, LukionOpiskeluoikeusjakso}
import org.scalatest.FreeSpec
import java.time.LocalDate.{of => date}

class OppijaValidationIBSpec extends FreeSpec with LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[IBOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[IBOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = opiskeluoikeus

  "IB validation" - {

    "IB tutkinnon suoritus" - {

      "Kaksi samaa oppiainetta"  - {
        def historiaOppiaine(level: String, arvosana: String) = ibAineSuoritus(ibOppiaine("HIS", level, 3), ibArviointi(arvosana, predicted = false))
        "Joilla sama taso" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "S"),
            historiaOppiaine(higherLevel, "1")
          ))
          "Palautetaan HTTP/400" in { putOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus oppiaineetib/HIS esiintyy useammin kuin kerran ryhmässä HL"))
          }}
        }

        "Eri taso, vain toisella tasolla numeroarviointi" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "2"),
            historiaOppiaine(standardLevel, "O")
          ))
          "Palautetaan HTTP/200" in { putOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }}
        }

        "Eri taso, molemmilla numeroarviointi" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "3"),
            historiaOppiaine(standardLevel, "4")
          ))
          "Palautetaa HTTP/400" in { putOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.kaksiSamaaOppiainettaNumeroarvioinnilla("Kahdella saman oppiaineen suorituksella oppiaineetib/HIS ei molemmilla voi olla numeerista arviointia"))
          }}
        }

        "Eri taso, ei numeroarviointeja" - {
          val opiskeluoikeus = opiskeluoikeusIBTutkinnollaWithOppiaineet(List(
            historiaOppiaine(higherLevel, "S"),
            historiaOppiaine(standardLevel, "S")
          ))
          "Palautetaan HTPP/200" in { putOpiskeluoikeus(opiskeluoikeus) {
            verifyResponseStatusOk()
          }}
        }
      }
    }

    "Opintojen rahoitus" - {
      "lasna -tilalta vaaditaan opintojen rahoitus" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = LukionOpiskeluoikeudenTila(List(LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta lasna puuttuu opintojen rahoitus"))
        }
      }
      "valmistunut -tilalta vaaditaan opintojen rahoitus" in {
        val tila = LukionOpiskeluoikeudenTila(List(
          LukionOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusValmistunut)
        ))
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.opintojenRahoitusPuuttuu("Opiskeluoikeuden tilalta valmistunut puuttuu opintojen rahoitus"))
        }
      }
    }
  }

  private def opiskeluoikeusIBTutkinnollaWithOppiaineet(oppiaineet: List[IBOppiaineenSuoritus]) = {
    defaultOpiskeluoikeus.copy(
      suoritukset = List(ibTutkinnonSuoritus(predicted = false).copy(
        osasuoritukset = Some(oppiaineet)
      ))
    )
  }
}
