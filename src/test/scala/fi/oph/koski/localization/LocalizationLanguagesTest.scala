package fi.oph.koski.localization

import fi.oph.koski.cache.GlobalCacheManager
import org.scalatest.{FreeSpec, Matchers, Tag}

/**
  * Tests that Swedish translations exist in the environment defined by the VIRKAILIJA_ROOT environment variable
  */
class LocalizationLanguagesTest extends FreeSpec with Matchers {
  "Kielistetyt tekstit" - {
    implicit lazy val cacheManager = GlobalCacheManager
    
    lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
    lazy val remoteLocalizations = new ReadOnlyRemoteLocalizationRepository(root).localizations
    lazy val localLocalizations = new MockLocalizationRepository().localizations

    s"Suomenkieliset tekstit" taggedAs(LocalizationTestTag) in {
      val missingKeys = localLocalizations.keySet -- remoteLocalizations.keySet

      missingKeys.toList.sorted shouldBe(empty)
    }

    s"Ruotsinkieliset tekstit" taggedAs(LocalizationTestTag) in {
      val ignoredKey = (key: String) => key.startsWith("description:")

      val missingKeys = localLocalizations.keySet.filterNot(ignoredKey) -- remoteLocalizations.filter(_._2.hasLanguage("sv")).keySet

      missingKeys.toList.sorted shouldBe(empty)
    }
  }
}

object LocalizationTestTag extends Tag("localization")