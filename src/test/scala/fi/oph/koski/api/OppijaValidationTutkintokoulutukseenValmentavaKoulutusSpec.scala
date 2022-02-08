package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExamplesTutkintokoulutukseenValmentavaKoulutus._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationTutkintokoulutukseenValmentavaKoulutusSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[TutkintokoulutukseenValmentavanOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[TutkintokoulutukseenValmentavanOpiskeluoikeus]]

  "Tutkintokoulutukseen valmentava koulutus" - {
    resetFixtures()

    "Suoritukset" - {
      "valmistuneen päätason suorituksen kesto ja osasuoritukset vaatimusten mukaiset" in {
        putOpiskeluoikeus(tuvaOpiskeluOikeusValmistunut, henkilö = tuvaHenkilö, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "valmistuneen päätason suorituksen laajuus liian pieni (ja osasuorituksia puuttuu)" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus.copy(
            koulutusmoduuli = tuvaPäätasonSuoritus.koulutusmoduuli.copy(
              laajuus = Some(LaajuusViikoissa(3))
            ),
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(1),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilö, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.tuvaPäätasonSuoritusVääräLaajuus())
        }
      }

      "valmistuneen päätason suorituksen osasuorituksen laajuus liian pieni" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus.copy(
            koulutusmoduuli = tuvaPäätasonSuoritus.koulutusmoduuli.copy(
              laajuus = Some(LaajuusViikoissa(4))
            ),
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(1),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(1),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilö, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.tuvaOpiskeluJaUrasuunnittelutaidotVääräLaajuus())
        }
      }

      "valmistuneen päätason suorituksesta puuttuu opiskelu ja urasuunnittelutaitojen osasuoritus" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus.copy(
            koulutusmoduuli = tuvaPäätasonSuoritus.koulutusmoduuli.copy(
              laajuus = Some(LaajuusViikoissa(4))
            ),
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilö, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu())
        }
      }

      "valmistuneen päätason suorituksesta puuttuu riittävä määrä eri osasuorituksia" in {
        val oo = tuvaOpiskeluOikeusValmistunut.copy(
          suoritukset = List(tuvaPäätasonSuoritus.copy(
            koulutusmoduuli = tuvaPäätasonSuoritus.koulutusmoduuli.copy(
              laajuus = Some(LaajuusViikoissa(4))
            ),
            osasuoritukset = Some(
              List(
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaOpiskeluJaUrasuunnittelutaidot(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                ),
                tuvaKoulutuksenMuunOsanSuoritus(
                  koulutusmoduuli = tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot(2),
                  koodistoviite = "tutkintokoulutukseenvalmentava",
                  arviointiPäivä = Some(date(2021, 9, 1))
                )
              )
            )
          ))
        )

        putOpiskeluoikeus(oo, henkilö = tuvaHenkilö, headers = authHeaders(stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tuvaOsasuorituksiaLiianVähän())
        }
      }
    }
  }

  override def defaultOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus = tuvaOpiskeluOikeusValmistunut
}
