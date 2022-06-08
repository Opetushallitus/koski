package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.varsinaisSuomiPalvelukäyttäjä
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}
import scala.Left

class OppijaValidationVapaaSivistystyöVapaatavoitteinenSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Vapaatavoitteinen" - {
    resetFixtures()

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

    "Suostumuksen peruutus" - {
      "Kun suostumus on peruutettu, oppijan opiskeluoikeuksia ei saa API:n kautta" in {
        val oppijaOid = KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid
        val oppijaResult = tryOppija(oppijaOid)
        oppijaResult.isLeft should be(true)
        oppijaResult.left.map(_.statusCode) should be(Left(404))
      }

      "Kun suostumus on peruutettu, oppijan opiskeluoikeuksia ei saa API:n kautta, vaikka olisi oikeudet mitätöityihin opiskeluoikeuksiin" in {
        val oppijaOid = KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid
        val oppijaResult = tryOppija(oppijaOid, MockUsers.paakayttajaMitatoidytOpiskeluoikeudet)
        oppijaResult.isLeft should be(true)
        oppijaResult.left.map(_.statusCode) should be(Left(404))
      }
    }

    "Suoritukset" - {
      "Kun mukana lähdejärjestelmäId, validoidaan" - {
        val lähdejärjestelmällinen = VapaatavoitteinenOpiskeluoikeus.copy(
          lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-423082"))
        )

        "Jos päätason suoritus vahvistettu, tilan tulee olla 'Hyväksytysti suoritettu'" in {
          putOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
          }
        }

        "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
          putOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
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
          putOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          putOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(kasvatettuVersiota)) {
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
      "Kun opiskeluoikeus on mitätöity" - {
        "Ei validointivirhettä, jos päätason suoritus vahvistettu, eikä tila ole 'Hyväksytysti suoritettu'" in {
          resetFixtures()
          val oo = putAndGetOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
        "Ei validointivirhettä, jos päätason suoritus vahvistamaton, eikä tila ole 'Keskeytynyt'" in {
          resetFixtures()
          val oo = putAndGetOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
        "Ei validointivirhettä, jos päätason suorituksella ei ole vähintään yhtä arvioitua osasuoritusta" in {
          resetFixtures()
          val oo = putAndGetOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): VapaanSivistystyönOpiskeluoikeus =
    putOpiskeluoikeus(oo) {
      verifyResponseStatusOk()
      getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
    }.asInstanceOf[VapaanSivistystyönOpiskeluoikeus]

  private def mitätöityOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(
        oo.tila.opiskeluoikeusjaksot ++
          List(
            VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusMitätöity)
          )
      )
    )
  }

  private def vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
        VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKeskeytynyt)
      ))
    )
  }

  private def vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
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
