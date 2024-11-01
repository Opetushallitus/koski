package fi.oph.koski.inenvironmentlocalization

import fi.oph.koski.TestEnvironment
import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.localization.{KoskiLocalizationConfig, MockLocalizationRepository, ReadOnlyRemoteLocalizationRepository}
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/**
  * Tests that Swedish translations exist in the environment defined by the VIRKAILIJA_ROOT environment variable
  */
class LocalizationLanguagesTest extends AnyFreeSpec with TestEnvironment with Matchers {
  "Kielistetyt tekstit" - {
    implicit lazy val cacheManager = GlobalCacheManager

    lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
    lazy val localizationConfig = sys.env.getOrElse("LOCALIZATION_CATEGORY", "koski") match {
      case "koski" => new KoskiLocalizationConfig
      case "valpas" => new ValpasLocalizationConfig
    }
    lazy val remoteLocalizations = new ReadOnlyRemoteLocalizationRepository(root, localizationConfig).localizations
    lazy val localLocalizations = new MockLocalizationRepository(localizationConfig).localizations

    s"Suomenkieliset tekstit" in {
      val missingKeys = localLocalizations.keySet -- remoteLocalizations.keySet

      missingKeys.toList.sorted shouldBe(empty)
    }

    s"Ruotsinkieliset tekstit" in {
      val ignoredKey = (key: String) => key.startsWith("description:") || eiTarvitseRuotsinkielistäKäännöstä.contains(key)

      val missingKeys = localLocalizations.keySet.filterNot(ignoredKey) -- remoteLocalizations.filter(_._2.hasLanguage("sv")).keySet

      missingKeys.toList.sorted shouldBe(empty)
    }
  }

  private val eiTarvitseRuotsinkielistäKäännöstä =
    List("Creativity action service", "Effort", "Extended essay", "Synteettinen", "Varoitukset").toSet
}
