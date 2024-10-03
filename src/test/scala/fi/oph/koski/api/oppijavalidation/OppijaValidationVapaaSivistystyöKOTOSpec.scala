package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.documentation.{ExamplesVapaaSivistystyöKotoutuskoulutus2022 => Koto2022}
import fi.oph.koski.http.{ErrorMatcher, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.validation.VSTKotoutumiskoulutus2022Validation
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class OppijaValidationVapaaSivistystyöKOTOSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "KOTO" - {
    "Päätason suorituksen laajuus lasketaan automaattisesti osasuoritusten laajuuksista" in {
      val opiskeluoikeus = opiskeluoikeusKOTO.withSuoritukset(List(
        suoritusKOTO.copy(
          vahvistus = None,
          osasuoritukset = Some(List(
            vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
            vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(laajuus = LaajuusOpintopisteissä(9))
          ))
        )))
      val result = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe (69)
    }

    "Ei voi tallentaa väärällä perusteen diaarinumerolla" in {
      val oo = opiskeluoikeusKOTO.withSuoritukset(List(
        suoritusKOTO.copy(
          koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(perusteenDiaarinumero = Some("OPH-5410-2021")) // ajoneuvoalan perustutkinto
        )
      ))
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
      }
    }
  }

  "KOTO 2022" - {
    "Päätason suorituksen ja osasuorituksen laajuudeet lasketaan automaattisesti (ala)osasuoritusten laajuuksista" in {
      val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu
      val result = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus)

      val suoritus = result.suoritukset.head
      val kieliopinnot = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "kielijaviestintaosaaminen")).get
      val yhteiskuntaopinnot = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "yhteiskuntajatyoelamaosaaminen")).get
      val ohjaus = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "ohjaus")).get
      val valinnaiset = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "valinnaisetopinnot")).get

      kieliopinnot.koulutusmoduuli.laajuusArvo(0) shouldBe (40)
      yhteiskuntaopinnot.koulutusmoduuli.laajuusArvo(0) shouldBe (20)
      ohjaus.koulutusmoduuli.laajuusArvo(0) shouldBe (7)
      valinnaiset.koulutusmoduuli.laajuusArvo(0) shouldBe (8)
      suoritus.koulutusmoduuli.laajuusArvo(0) shouldBe (75)
    }

    "Automaattinen opintopisteiden laskenta ylikirjoittaa ylemmän osasuoritustason virheellisen opintopistemäärän" in {
      val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu.copy(
        suoritukset = List(Koto2022.PäätasonSuoritus.suoritettu.copy(
          koulutusmoduuli = Koto2022.PäätasonSuoritus.suoritettu.koulutusmoduuli.copy(
            laajuus = Some(LaajuusOpintopisteissä(666))
          )
        ))
      )

      val result = setupOppijaWithAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe (75)
    }

    "Rajapäivät" - {
      "Uuden opsin mukaista opiskeluoikeutta, joka alkaa ennen 1.8.2022, ei voi siirtää" in {
        val opiskeluoikeus = Koto2022.Opiskeluoikeus.keskeneräinen.copy(
          tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 7, 31), opiskeluoikeusLäsnä)
          )),
        )

        setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2022())
        }
      }

      "Vanhan opsin mukaista opiskeluoikeutta, joka alkaa 1.8.2022 tai myöhemmin, ei voi siirtää" in {
        val opiskeluoikeus = opiskeluoikeusKOTO.copy(
          tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
            OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 8, 1), opiskeluoikeusLäsnä)
          )),
          arvioituPäättymispäivä = Some(LocalDate.of(2022, 9, 1)),
        )

        setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2012())
        }
      }
    }

    "Ei voi tallentaa väärällä perusteen diaarinumerolla" in {
      val oo = Koto2022.Opiskeluoikeus.suoritettu.copy(
        suoritukset = List(
          Koto2022.PäätasonSuoritus.suoritettu.copy(
            koulutusmoduuli = VSTKotoutumiskoulutus2022(perusteenDiaarinumero = Some("OPH-5410-2021")) // ajoneuvoalan perustutkinto
          )
        ))

      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*notAnyOf.*".r))
      }
    }
  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): Opiskeluoikeus = setupOppijaWithOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOTO
}
