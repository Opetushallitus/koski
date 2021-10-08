package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.varsinaisSuomiPalvelukäyttäjä
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyöVapaatavoitteinenSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Vapaatavoitteinen" - {
    "Opiskeluoikeuden tila" - {
      "Opiskeluoikeuden tila ei voi olla 'katsotaaneronneeksi'" in {
        val oo = VapaatavoitteinenOpiskeluoikeus.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKatsotaanEronneeksi)
            )
          )
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila())
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'valmistunut'" in {
        val oo = VapaatavoitteinenOpiskeluoikeus.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusValmistunut)
            )
          )
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila())
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'lasna'" in {
        val oo = VapaatavoitteinenOpiskeluoikeus.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusLäsnä)
            )
          )
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila())
        }
      }
    }

    "Suoritukset" - {
      "Kun mukana lähdejärjestelmäId, validoidaan" - {
        val lähdejärjestelmällinen = VapaatavoitteinenOpiskeluoikeus.copy(
          lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId)
        )

        "Jos päätason suoritus vahvistettu, tilan tulee olla 'Hyväksytysti suoritettu'" in {
          putOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
          }
        }

        "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
          putOpiskeluoikeus(vahvistamatonPäätasaonSuoritusHyväksyttyOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"))
          }
        }

        "Päätason suorituksella tulee olla vähintään yksi arvioitu osasuoritus" in {
          putOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenPäätasonOsasuoritukset())
          }
        }
      }
      "Kun mukana ei ole lähdejärjestelmäId:tä, ensimmäistä versiota ei validoida" - {
        val kasvatettuVersiota = VapaatavoitteinenOpiskeluoikeus.copy(
          versionumero = Some(2)
        )

        "Jos päätason suoritus vahvistettu, tilan tulee olla 'Hyväksytysti suoritettu'" in {
          putOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          putOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
          }
        }

        "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
          putOpiskeluoikeus(vahvistamatonPäätasaonSuoritusHyväksyttyOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          putOpiskeluoikeus(vahvistamatonPäätasaonSuoritusHyväksyttyOpiskeluoikeus(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"))
          }
        }

        "Päätason suorituksella tulee olla vähintään yksi arvioitu osasuoritus" in {
          putOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          putOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenPäätasonOsasuoritukset())
          }
        }
      }
    }
  }

  private def vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
        VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKeskeytynyt)
      ))
    )
  }

  private def vahvistamatonPäätasaonSuoritusHyväksyttyOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      suoritukset = List(suoritusVapaatavoitteinenKoulutus.copy(
        vahvistus = None
      )))
  }

  private def päätasoArvioimattomallaOsasuorituksella(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      suoritukset = List(suoritusVapaatavoitteinenKoulutus.copy(
        osasuoritukset = Some(List(
          vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.copy(
            arviointi = None
          )
        )))
      ))
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOPS
  def KOTOOPiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOTO
  def VapaatavoitteinenOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinen
}
