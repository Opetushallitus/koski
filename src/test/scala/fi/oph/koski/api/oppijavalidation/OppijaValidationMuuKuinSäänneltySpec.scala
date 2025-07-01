package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.ExamplesMuuKuinSäänneltyKoulutus
import fi.oph.koski.documentation.ExamplesVapaaSivistystyöJotpa.rahoitusJotpa
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class OppijaValidationMuuKuinSäänneltySpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[MuunKuinSäännellynKoulutuksenOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[MuunKuinSäännellynKoulutuksenOpiskeluoikeus]]

  "Muu kuin säännelty koulutus" - {
    resetFixtures()

    "Opiskeluoikeuden tila" - {
      "Opiskeluoikeuden tila ei voi olla 'katsotaaneronneeksi'" in {
        val oo = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusKatsotaanEronneeksi))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
        }
      }

      "Opiskeluoikeuden tila ei voi olla 'valmistunut'" in {
        val oo = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusValmistunut))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*enumValueMismatch.*".r))
        }
      }
    }

    "Rahoitusmuoto" - {
      "Läsnä-tilaista opiskeluoikeutta ei voi tallentaa ilman rahoitusmuotoa" in {
        val oo = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusLäsnä), None),
          lisätiedot = None,
        )
        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta lasna puuttuu rahoitusmuoto"))
        }
      }

      "Suoritettu-tilaista opiskeluoikeutta ei voi tallentaa ilman rahoitusmuotoa" in {
        val oo = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.suoritettu.copy(
          tila = opiskeluoikeudenTila(List(opiskeluoikeusHyväksytystiSuoritettu), None),
          lisätiedot = None,
        )
        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto("Opiskeluoikeuden tilalta hyvaksytystisuoritettu puuttuu rahoitusmuoto"))
        }
      }
    }

    "Duplikaatit opiskeluoikeudet" - {
      "Vastaavaa opiskeluoikeutta ei voi lisätä kahdesti" in {
        setupOppijaWithOpiskeluoikeus(ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken, defaultHenkilö){
          verifyResponseStatusOk()
        }

        postOpiskeluoikeus(ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken, defaultHenkilö){
          verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
        }
      }
      "Vastaavan opiskeluoikeuden voi lisätä, kun opiskeluoikeuksien voimassaolot eivät ole ajallisesti päällekkäin" in {
        setupOppijaWithOpiskeluoikeus(ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.suoritettu, defaultHenkilö){
          verifyResponseStatusOk()
        }

        val ooAlkaaMyöhemmin = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
          tila = MuunKuinSäännellynKoulutuksenTila(List(
            ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.OpiskeluoikeudenJakso.läsnä(LocalDate.of(2023, 2, 2))
          ))
        )
        postOpiskeluoikeus(ooAlkaaMyöhemmin, defaultHenkilö){
          verifyResponseStatusOk()
        }
      }
      "Vastaavan opiskeluoikeuden voi lisätä, vaikka sen voimassaolo on ajallisesti päällekkäin, kun opintokokonaisuus on eri" in {
        val ooSarjakuvailmaisu = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
          suoritukset = List(
            ExamplesMuuKuinSäänneltyKoulutus.PäätasonSuoritus.suoritusIlmanOsasuorituksia.copy(
              koulutusmoduuli = MuuKuinSäänneltyKoulutus(
                opintokokonaisuus = Koodistokoodiviite("1139", None, "opintokokonaisuudet", Some(1))
              )
            )
          )
        )

        setupOppijaWithOpiskeluoikeus(ooSarjakuvailmaisu, defaultHenkilö){
          verifyResponseStatusOk()
        }

        postOpiskeluoikeus(ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken, defaultHenkilö){
          verifyResponseStatusOk()
        }
      }
    }
  }

  override def defaultOpiskeluoikeus: MuunKuinSäännellynKoulutuksenOpiskeluoikeus = ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken

  def opiskeluoikeudenTila(
    tilat: List[Koodistokoodiviite],
    opintojenRahoitus: Option[Koodistokoodiviite] = Some(rahoitusJotpa),
    aloitusPvm: LocalDate = LocalDate.of(2023, 1, 1),
  ): MuunKuinSäännellynKoulutuksenTila =
    MuunKuinSäännellynKoulutuksenTila(tilat.zipWithIndex.map {
      case (tila, index) => MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(
        alku = aloitusPvm.plusMonths(index),
        tila = tila,
        opintojenRahoitus = opintojenRahoitus,
      )
    })
}
