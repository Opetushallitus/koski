package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.AmmatillinenExampleData.{opiskeluoikeus, primusLähdejärjestelmäId, winnovaLähdejärjestelmäId}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValiaikaisestiKeskeytynyt, opiskeluoikeusValmistunut, vahvistus}
import fi.oph.koski.documentation.ExamplesVapaaSivistystyöJotpa
import fi.oph.koski.documentation.ExamplesVapaaSivistystyöJotpa.rahoitusJotpa
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.varsinaisSuomiPalvelukäyttäjä
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyöJotpaSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Jatkuvaan oppimiseen suunnattu" - {
    resetFixtures()

    "Opiskeluoikeuden tila" - {
      "Opiskeluoikeuden tila ei voi olla 'katsotaaneronneeksi'" in {
        val oo = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinen.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2023, 5, 31), opiskeluoikeusKatsotaanEronneeksi)
            )
          )
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'valmistunut'" in {
        val oo = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinen.withTila(
          VapaanSivistystyönOpiskeluoikeudenTila(
            List(
              OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2023, 5, 31), opiskeluoikeusValmistunut)
            )
          )
        )

        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
        }
      }
    }

    "Suoritukset" - {
      val opiskeluoikeus = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.suoritettu.copy(
        lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-423082"))
      )

      "Happy path" in {
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "Jos päätason suoritus vahvistettu, tilan tulee olla 'Hyväksytysti suoritettu'" in {
        val oo = opiskeluoikeus.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusLäsnä, opiskeluoikeusKeskeytynyt)),
        )
        putOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistetulla jatkuvaan oppimiseen suunnatulla vapaan sivistystyön koulutuksella ei voi olla päättävänä tilana 'Keskeytynyt'"))
        }
      }

      "Jos päätason suoritus vahvistettu, tulee suorituksen, osasuoritusten sekä alaosasuoritusten laajuudet olla syötetty" in {
        val oo = opiskeluoikeus.copy(
          suoritukset = List(ExamplesVapaaSivistystyöJotpa.PäätasonSuoritus.suoritettu.copy(
            koulutusmoduuli = ExamplesVapaaSivistystyöJotpa.PäätasonSuoritus.suoritettu.koulutusmoduuli.copy(
              laajuus = None
            ),
            osasuoritukset = Some(List(
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus1,
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus2,
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus3Arvioitu.copy(
                koulutusmoduuli = ExamplesVapaaSivistystyöJotpa.Osasuoritus.Koulutusmoduuli.kurssi3EiLaajuutta,
                osasuoritukset = Some(List(
                  ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus3Arvioitu.copy(
                    koulutusmoduuli = VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
                      tunniste = PaikallinenKoodi(nimi = LocalizedString.finnish("Johdatus tussitukseen"), koodiarvo = "1138-3-1"),
                      laajuus = None
                    )
                  )
                ))
              )
            ))
          ))
        )
        putOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.laajuudet("Vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen suoritukselta koulutus/099999 puuttuu laajuus"),
            KoskiErrorCategory.badRequest.validation.laajuudet("Vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen osasuoritukselta 1138-3 (Tussitekniikat I ja II) puuttuu laajuus"),
            KoskiErrorCategory.badRequest.validation.laajuudet("Vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen osasuoritukselta 1138-3-1 (Johdatus tussitukseen) puuttuu laajuus")
          )
        }
      }

      "Jos päätason suoritus ei ole vahvistettu, ei vaadita laajuuksia suorituksilta, osasuorituksilta tai alaosasuorituksilta" in {
        val oo = opiskeluoikeus.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusLäsnä)),
          suoritukset = List(ExamplesVapaaSivistystyöJotpa.PäätasonSuoritus.suoritettu.copy(
            vahvistus = None,
            koulutusmoduuli = ExamplesVapaaSivistystyöJotpa.PäätasonSuoritus.suoritettu.koulutusmoduuli.copy(
              laajuus = None
            ),
            osasuoritukset = Some(List(
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus1,
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus2,
              ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus3Arvioitu.copy(
                koulutusmoduuli = ExamplesVapaaSivistystyöJotpa.Osasuoritus.Koulutusmoduuli.kurssi3EiLaajuutta,
                osasuoritukset = Some(List(
                  ExamplesVapaaSivistystyöJotpa.Osasuoritus.osasuoritus3Arvioitu.copy(
                    koulutusmoduuli = ExamplesVapaaSivistystyöJotpa.Osasuoritus.Koulutusmoduuli.kurssi3EiLaajuutta
                  )
                ))
              )
            ))
          ))
        )
        putOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "Jos päätason suoritus vahvistamaton, tilan tulee olla 'Keskeytynyt'" in {
        val oo = opiskeluoikeus.copy(
          suoritukset = List(opiskeluoikeus.suoritukset.head.asInstanceOf[VapaanSivistystyönJotpaKoulutuksenSuoritus].copy(
            vahvistus = None,
          ))
        )
        putOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteeisenKoulutuksenVahvistus("Vahvistamattomalla jatkuvaan oppimiseen suunnatulla vapaan sivistystyön koulutuksella ei voi olla päättävänä tilana 'Hyväksytysti suoritettu'"))
        }
      }

      "Kun opiskeluoikeus on mitätöity" - {
        "Ei validointivirhettä, jos päätason suoritus vahvistettu, eikä tila ole 'Hyväksytysti suoritettu'" in {
          resetFixtures()
          val oo = putAndGetOpiskeluoikeus(ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.suoritettu.copy(
            tila = opiskeluoikeudenTila(List(opiskeluoikeusLäsnä))
          ))

          putOpiskeluoikeus(mitätöityOpiskeluoikeus(oo)) {
            verifyResponseStatusOk()
          }
        }
      }
    }

    "Rahoitus" - {
      "Rahoitusmuoto pitää olla ilmoitettu opiskeluoikeuden tilalle läsnä" in {
        val oo = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinen.copy(tila = opiskeluoikeudenTila(List(opiskeluoikeusLäsnä), None))
        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto())
        }
      }

      "Rahoitusmuoto pitää olla ilmoitettu opiskeluoikeuden tilalle hyväksytysti suoritettu" in {
        val oo = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.suoritettu.copy(tila = opiskeluoikeudenTila(List(opiskeluoikeusHyväksytystiSuoritettu), None, LocalDate.of(2023, 2, 1)))
        putOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto())
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
            VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
              alku = date(2023, 5, 31),
              tila = opiskeluoikeusMitätöity,
              opintojenRahoitus = None,
            )
          )
      )
    )
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = ExamplesVapaaSivistystyöJotpa.Opiskeluoikeus.keskeneräinen

  def opiskeluoikeudenTila(
    tilat: List[Koodistokoodiviite],
    opintojenRahoitus: Option[Koodistokoodiviite] = Some(rahoitusJotpa),
    aloitusPvm: LocalDate = LocalDate.of(2023, 1, 1),
  ): VapaanSivistystyönOpiskeluoikeudenTila =
    VapaanSivistystyönOpiskeluoikeudenTila(tilat.zipWithIndex.map {
      case (tila, index) => VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
        alku = aloitusPvm.plusMonths(index),
        tila = tila,
        opintojenRahoitus = opintojenRahoitus,
      )
    })
}
