package fi.oph.common.localization

import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.config.LocalizationConfig
import org.scalatest.{FreeSpec, Matchers}

/**
  * Tests that Swedish translations exist in the environment defined by the VIRKAILIJA_ROOT environment variable
  */
class LocalizationLanguagesTest extends FreeSpec with Matchers {
  "Kielistetyt tekstit" - {
    implicit lazy val cacheManager = GlobalCacheManager

    lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
    lazy val localizationCategory = sys.env.getOrElse("LOCALIZATION_CATEGORY", "koski")
    lazy val remoteLocalizations = new ReadOnlyRemoteLocalizationRepository(root, LocalizationConfig(localizationCategory)).localizations
    lazy val localLocalizations = new MockLocalizationRepository(LocalizationConfig(localizationCategory)).localizations

    s"Suomenkieliset tekstit" taggedAs(LocalizationTestTag) in {
      val missingKeys = localLocalizations.keySet -- remoteLocalizations.keySet

      missingKeys.toList.sorted shouldBe(empty)
    }

    s"Ruotsinkieliset tekstit" taggedAs(LocalizationTestTag) in {
      val ignoredKey = (key: String) => key.startsWith("description:") || eiTarvitseRuotsinkielistäKäännöstä.contains(key)

      val missingKeys = localLocalizations.keySet.filterNot(ignoredKey) -- remoteLocalizations.filter(_._2.hasLanguage("sv")).keySet

      missingKeys.toList.sorted shouldBe(empty)
    }
  }

  private val eiTarvitseRuotsinkielistäKäännöstä =
    List("Creativity action service", "Effort", "Extended essay", "Synteettinen", "Varoitukset").toSet
}
