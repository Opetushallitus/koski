package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.ConfigFactory
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AhvenanmaanPerusopetusExampleData
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationAhvenanmaanPerusopetusSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with PutOpiskeluoikeusTestMethods[AhvenanmaanPerusopetuksenOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = AhvenanmaanPerusopetusExampleData.opiskeluoikeus

  "Ahvenanmaan perusopetuksen opiskeluoikeus" - {
    "voidaan tallentaa paikallisesti" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "tuotantokonfiguraatio estää tallennuksen disabledPäätasonSuoritusLuokat-asetuksella" in {
      implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
      implicit val accessType: AccessType.Value = AccessType.write
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = []
            disabledPäätasonSuoritusLuokat = [
              AhvenanmaanPerusopetuksenVuosiluokanSuoritus
              AhvenanmaanPerusopetuksenOppimääränSuoritus
              AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
            ]
            disabledOsasuoritusTyypit = []
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val validator = new KoskiValidator(
        KoskiApplicationForTests.organisaatioRepository,
        KoskiApplicationForTests.possu,
        KoskiApplicationForTests.henkilöRepository,
        new EPerusteisiinPerustuvaValidator(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu,
          config
        ),
        KoskiApplicationForTests.ePerusteetLops2019Validator,
        new EPerusteetFiller(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu
        ),
        KoskiApplicationForTests.validatingAndResolvingExtractor,
        KoskiApplicationForTests.suostumuksenPeruutusService,
        KoskiApplicationForTests.koodistoViitePalvelu,
        config,
        KoskiApplicationForTests.validationContext,
      )

      val opiskelija = oppija(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas.oid)
      val result = validator.updateFieldsAndValidateAsJson(opiskelija)
      result.swap.toOption.get should equal(
        KoskiErrorCategory.notImplemented("Päätason suorituksen luokka AhvenanmaanPerusopetuksenVuosiluokanSuoritus ei ole käytössä tässä ympäristössä")
      )

      val aikuisopiskelija = oppija(KoskiSpecificMockOppijat.ahvenanmaanAikuisopiskelija.oid)
      val aikuistenResult = validator.updateFieldsAndValidateAsJson(aikuisopiskelija)
      aikuistenResult.swap.toOption.get should equal(
        KoskiErrorCategory.notImplemented("Päätason suorituksen luokka AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus ei ole käytössä tässä ympäristössä")
      )
    }
  }

  "Ahvenanmaan perusopetuksen oppimäärä muille kuin oppivelvollisille" - {
    val aikuistenOpiskeluoikeus = AhvenanmaanPerusopetusExampleData.aikuistenOpiskeluoikeus

    "voidaan tallentaa paikallisesti" in {
      setupOppijaWithOpiskeluoikeus(aikuistenOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "opiskeluoikeudella ei saa olla vuosiluokkasuorituksia" in {
      val opiskeluoikeus = aikuistenOpiskeluoikeus.copy(
        suoritukset = List(
          AhvenanmaanPerusopetusExampleData.aikuistenPäättötodistuksenSuoritus,
          AhvenanmaanPerusopetusExampleData.kahdeksannenLuokanSuoritus
        )
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          "Ahvenanmaan perusopetuksen opiskeluoikeudella, jolla on muiden kuin oppivelvollisten oppimäärän suoritus, ei voi olla vuosiluokan suorituksia"
        ))
      }
    }

    "suoritukselta vaaditaan alkamispäivä" in {
      val opiskeluoikeus = aikuistenOpiskeluoikeus.copy(
        suoritukset = List(
          AhvenanmaanPerusopetusExampleData.aikuistenPäättötodistuksenSuoritus.copy(alkamispäivä = None)
        )
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu(
          "Suoritukselle koulutus/201101 ei ole merkitty alkamispäivää"
        ))
      }
    }
  }
}
