package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.ExamplesMuuKuinSäänneltyKoulutus
import fi.oph.koski.documentation.ExamplesMuuKuinSäänneltyKoulutus.PäätasonSuoritus.Osasuoritus.{Arviointi, Koulutusmoduuli}
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

    "Osasuorituksen arviointi" - {
      "Arviointi vaaditaan vahvistetun suorituksen osasuoritukselta läsnä-tilaisessa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arvioitu = false,
          vahvistettu = true,
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointi"
          ))
        }
      }

      "Arviointi vaaditaan vahvistetun suorituksen osasuoritukselta suoritettu-tilaisessa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arvioitu = false,
          vahvistettu = true,
          päättäväTila = Some(opiskeluoikeusHyväksytystiSuoritettu),
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointi"
          ))
        }
      }

      "Arvioidut osa- ja alaosasuoritukset sallitaan vahvistetussa suorituksessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          alaosasuorituksena = true,
          vahvistettu = true,
          päättäväTila = Some(opiskeluoikeusHyväksytystiSuoritettu),
        )) {
          verifyResponseStatusOk()
        }
      }

      "Arviointia ei vaadita, kun suoritusta ei ole vahvistettu" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arvioitu = false,
        )) {
          verifyResponseStatusOk()
        }
      }

      "Arviointia ei vaadita ennen 1.1.2027 alkaneessa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2026, 12, 31),
          arvioitu = false,
          vahvistettu = true,
        )) {
          verifyResponseStatusOk()
        }
      }

      "Arviointi vaaditaan myös alaosasuoritukselta" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arvioitu = false,
          alaosasuorituksena = true,
          vahvistettu = true,
          päättäväTila = Some(opiskeluoikeusHyväksytystiSuoritettu),
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointi"
          ))
        }
      }

      "Arviointi vaaditaan myös osasuoritukselta, jolla on alaosasuorituksia" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          alaosasuorituksena = true,
          ylempiOsasuoritusArvioitu = false,
          vahvistettu = true,
          päättäväTila = Some(opiskeluoikeusHyväksytystiSuoritettu),
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Grafiikka puuttuu arviointi"
          ))
        }
      }

      "Keskeytyneessä opiskeluoikeudessa" - {
        "Osasuoritus, jolla on alaosasuorituksia, voi olla ilman arviointia" in {
          setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
            alkamispäivä = LocalDate.of(2027, 1, 1),
            alaosasuorituksena = true,
            ylempiOsasuoritusArvioitu = false,
            vahvistettu = true,
            päättäväTila = Some(opiskeluoikeusKeskeytynyt),
          )) {
            verifyResponseStatusOk()
          }
        }

        "Arviointi vaaditaan alaosasuoritukselta" in {
          setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
            alkamispäivä = LocalDate.of(2027, 1, 1),
            arvioitu = false,
            alaosasuorituksena = true,
            vahvistettu = true,
            päättäväTila = Some(opiskeluoikeusKeskeytynyt),
          )) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
              "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointi"
            ))
          }
        }

        "Arviointi vaaditaan osasuoritukselta, jolla ei ole alaosasuorituksia" in {
          setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
            alkamispäivä = LocalDate.of(2027, 1, 1),
            arvioitu = false,
            vahvistettu = true,
            päättäväTila = Some(opiskeluoikeusKeskeytynyt),
          )) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(
              "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointi"
            ))
          }
        }
      }
    }

    "Osasuorituksen arviointipäivä" - {
      "Arviointipäivä vaaditaan 1.1.2027 alkavassa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arviointipäivä = None,
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointipäiväPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointipäivä"
          ))
        }
      }

      "Arviointipäivää ei vaadita ennen 1.1.2027 alkaneessa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2026, 12, 31),
          arviointipäivä = None,
        )) {
          verifyResponseStatusOk()
        }
      }

      "Päivämäärällinen arviointi sallitaan 1.1.2027 alkavassa opiskeluoikeudessa" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arviointipäivä = Some(LocalDate.of(2027, 2, 1)),
        )) {
          verifyResponseStatusOk()
        }
      }

      "Arviointipäivä vaaditaan myös arvioidulta alaosasuoritukselta" in {
        setupOppijaWithOpiskeluoikeus(muksOpiskeluoikeusOsasuorituksella(
          alkamispäivä = LocalDate.of(2027, 1, 1),
          arviointipäivä = None,
          alaosasuorituksena = true,
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.arviointipäiväPuuttuu(
            "Muun kuin säännellyn koulutuksen osasuoritukselta Maalaus puuttuu arviointipäivä"
          ))
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

  private def muksOpiskeluoikeusOsasuorituksella(
    alkamispäivä: LocalDate,
    arviointipäivä: Option[LocalDate] = Some(LocalDate.of(2027, 2, 1)),
    arvioitu: Boolean = true,
    alaosasuorituksena: Boolean = false,
    ylempiOsasuoritusArvioitu: Boolean = true,
    vahvistettu: Boolean = false,
    päättäväTila: Option[Koodistokoodiviite] = None,
  ): MuunKuinSäännellynKoulutuksenOpiskeluoikeus = {
    val maalaus = MuunKuinSäännellynKoulutuksenOsasuoritus(
      koulutusmoduuli = Koulutusmoduuli.maalaus(10.0),
      arviointi = if (arvioitu) Some(List(Arviointi.arvosana().copy(arviointipäivä = arviointipäivä))) else None,
    )
    val osasuoritus = if (alaosasuorituksena) {
      MuunKuinSäännellynKoulutuksenOsasuoritus(
        koulutusmoduuli = Koulutusmoduuli.grafiikka(10.0),
        arviointi = if (ylempiOsasuoritusArvioitu) Some(List(Arviointi.arvosana(pvm = LocalDate.of(2027, 2, 1)))) else None,
        osasuoritukset = Some(List(maalaus)),
      )
    } else {
      maalaus
    }

    ExamplesMuuKuinSäänneltyKoulutus.Opiskeluoikeus.kesken.copy(
      tila = opiskeluoikeudenTila(opiskeluoikeusLäsnä :: päättäväTila.toList, aloitusPvm = alkamispäivä),
      suoritukset = List(
        ExamplesMuuKuinSäänneltyKoulutus.PäätasonSuoritus.suoritusIlmanOsasuorituksia.copy(
          vahvistus = if (vahvistettu) Some(Päivämäärävahvistus(
            päivä = alkamispäivä.plusMonths(1),
            myöntäjäOrganisaatio = ExamplesMuuKuinSäänneltyKoulutus.jatkuvaKoulutusOyOppilaitos,
          )) else None,
          osasuoritukset = Some(List(osasuoritus)),
        )
      ),
    )
  }

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
