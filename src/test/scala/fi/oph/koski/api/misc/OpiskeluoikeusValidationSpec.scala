package fi.oph.koski.api.misc

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetLops2019Validator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.parse
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, MockUsers}
import fi.oph.koski.opiskeluoikeus.ValidationResult
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeusValidationSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec {
  implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
  override def defaultUser = MockUsers.paakayttaja

  "Validoi" - {
    "validi opiskeluoikeus" in {
      val opiskeluoikeusOid = oppija(KoskiSpecificMockOppijat.eero.oid).tallennettavatOpiskeluoikeudet.flatMap(_.oid).head
      authGet(s"api/opiskeluoikeus/validate/$opiskeluoikeusOid") {
        verifyResponseStatusOk()
        validationResult.errors should be(empty)
      }
    }

    "Päätason suorituksen tyyppi jonka käyttö on estetty" in {
      implicit val accessType = AccessType.read
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = [
              valma
            ]
            disabledPäätasonSuoritusLuokat = [
            ]
            disabledOsasuoritusTyypit = [
            ]
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val opiskelija = oppija(KoskiSpecificMockOppijat.valma.oid)
      mockKoskiValidator(config).updateFieldsAndValidateAsJson(opiskelija).left.get should equal (KoskiErrorCategory.notImplemented("Päätason suorituksen tyyppi valma ei ole käytössä tässä ympäristössä"))
    }

    "Päätason suorituksen luokka jonka käyttö on estetty" in {
      implicit val accessType = AccessType.read
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = [
            ]
            disabledPäätasonSuoritusLuokat = [
              ValmaKoulutuksenSuoritus
            ]
            disabledOsasuoritusTyypit = [
            ]
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val opiskelija = oppija(KoskiSpecificMockOppijat.valma.oid)
      mockKoskiValidator(config).updateFieldsAndValidateAsJson(opiskelija).left.get should equal (KoskiErrorCategory.notImplemented("Päätason suorituksen luokka ValmaKoulutuksenSuoritus ei ole käytössä tässä ympäristössä"))
    }

    "Osasuorituksen tyyppi jonka käyttö on estetty" in {
      implicit val accessType = AccessType.read
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = [
            ]
            disabledPäätasonSuoritusLuokat = [
            ]
            disabledOsasuoritusTyypit = [
              valmakoulutuksenosa
            ]
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val opiskelija = oppija(KoskiSpecificMockOppijat.valma.oid)
      mockKoskiValidator(config).updateFieldsAndValidateAsJson(opiskelija).left.get should equal (KoskiErrorCategory.notImplemented("Osasuorituksen tyyppi valmakoulutuksenosa ei ole käytössä tässä ympäristössä"))
    }
  }

  private def validationResult = parse[ValidationResult](body)

  private def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      new EPerusteisiinPerustuvaValidator(
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu,
        config
      ),
      new EPerusteetLops2019Validator(KoskiApplicationForTests.config, KoskiApplicationForTests.ePerusteet),
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
  }
}
