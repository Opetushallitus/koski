package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers
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
              OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKatsotaanEronneeksi)
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'valmistunut'" in {
        val oo = VapaatavoitteinenOpiskeluoikeus.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusValmistunut)
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'lasna'" in {
        val oo = VapaatavoitteinenOpiskeluoikeus.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusLäsnä)
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }
    }

    "Opiskeluoikeuden opintokokonaisuus ei voi olla tyhjä" in {
      val oo = VapaatavoitteinenOpiskeluoikeusIlmanOpintokokonaisuutta
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.puuttuvaOpintokokonaisuus())
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
          setupOppijaWithOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
          }
        }

        "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
          setupOppijaWithOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"))
          }
        }

        "Päätason suorituksella tulee olla vähintään yksi arvioitu osasuoritus" in {
          setupOppijaWithOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(lähdejärjestelmällinen), headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenPäätasonOsasuoritukset())
          }
        }
      }
      "Kun mukana ei ole lähdejärjestelmäId:tä, ensimmäistä versiota ei validoida" - {
        val kasvatettuVersiota = VapaatavoitteinenOpiskeluoikeus.copy(
          versionumero = Some(2)
        )

        "Jos päätason suoritus vahvistettu, tilan tulee olla 'Hyväksytysti suoritettu'" in {
          setupOppijaWithOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          setupOppijaWithOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
          }
        }

        "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
          setupOppijaWithOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          setupOppijaWithOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla vapaan sivistystyön vapaatavoitteisella koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"))
          }
        }

        "Päätason suorituksella tulee olla vähintään yksi arvioitu osasuoritus" in {
          setupOppijaWithOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(VapaatavoitteinenOpiskeluoikeus)) {
            verifyResponseStatusOk()
          }
          setupOppijaWithOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(kasvatettuVersiota)) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenPäätasonOsasuoritukset())
          }
        }
      }
      "Kun opiskeluoikeus on mitätöity" - {
        "Ei validointivirhettä, jos päätason suoritus vahvistettu, eikä tila ole 'Hyväksytysti suoritettu'" in {
          val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
        "Ei validointivirhettä, jos päätason suoritus vahvistamaton, eikä tila ole 'Keskeytynyt'" in {
          val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistamatonPäätasonSuoritusHyväksyttyOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
        "Ei validointivirhettä, jos päätason suorituksella ei ole vähintään yhtä arvioitua osasuoritusta" in {
          val oo = setupOppijaWithAndGetOpiskeluoikeus(päätasoArvioimattomallaOsasuorituksella(VapaatavoitteinenOpiskeluoikeus))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
      }
    }

    "Duplikaatit opiskeluoikeudet" - {
      "Vastaavaa opiskeluoikeutta ei voi lisätä kahdesti" in {
        setupOppijaWithOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus, defaultHenkilö){
          verifyResponseStatusOk()
        }

        postOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus, defaultHenkilö){
          verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
        }
      }
      "Vastaavan opiskeluoikeuden voi lisätä, kun opiskeluoikeuksien voimassaolot eivät ole ajallisesti päällekkäin" in {
        val ooPäättynytMyöhemmin = VapaatavoitteinenOpiskeluoikeus.copy(
          arvioituPäättymispäivä = Some(date(2022, 6, 1)),
          tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
            VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(date(2022, 6, 1), opiskeluoikeusHyväksytystiSuoritettu)
          ))
        )

        setupOppijaWithOpiskeluoikeus(ooPäättynytMyöhemmin, defaultHenkilö){
          verifyResponseStatusOk()
        }

        // Päättynyt aiemmin
        postOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus, defaultHenkilö){
          verifyResponseStatusOk()
        }
      }
      "Vastaavan opiskeluoikeuden voi lisätä, vaikka sen voimassaolo on ajallisesti päällekkäin, kun opintokokonaisuus on eri" in {
        val ooSarjakuvailmaisu = VapaatavoitteinenOpiskeluoikeus.copy(
          suoritukset = List(
            suoritusVapaatavoitteinenKoulutus.copy(
              koulutusmoduuli = VapaanSivistystyönVapaatavoitteinenKoulutus(
                laajuus = Some(LaajuusOpintopisteissä(5)),
                opintokokonaisuus = Some(Koodistokoodiviite("1139", None, "opintokokonaisuudet", Some(1)))
              ),
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(ooSarjakuvailmaisu, defaultHenkilö){
          verifyResponseStatusOk()
        }

        postOpiskeluoikeus(VapaatavoitteinenOpiskeluoikeus, defaultHenkilö){
          verifyResponseStatusOk()
        }
      }
    }
  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): VapaanSivistystyönOpiskeluoikeus =
    setupOppijaWithOpiskeluoikeus(oo) {
      verifyResponseStatusOk()
      getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
    }.asInstanceOf[VapaanSivistystyönOpiskeluoikeus]

  private def mitätöityOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(
        oo.tila.opiskeluoikeusjaksot ++
          List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusMitätöity)
          )
      )
    )
  }

  private def vahvistettuPäätasonSuoritusKeskeytynytOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus) = {
    oo.copy(
      tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
        OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKeskeytynyt)
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
  def VapaatavoitteinenOpiskeluoikeusIlmanOpintokokonaisuutta: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinenIlmanOpintokokonaisuutta
}
