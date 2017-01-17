package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, CachingProxy}

object KoodistoPalvelu {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager) = {
    cached(withoutCache(config))
  }

  def cached(palvelu: KoodistoPalvelu)(implicit cacheInvalidator: CacheManager) = CachingProxy(Cache.cacheAllRefresh("KoodistoPalvelu", 3600, 100), palvelu)

  def withoutCache(config: Config): KoodistoPalvelu = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" => MockKoodistoPalvelu()
      case url => new RemoteKoodistoPalvelu(url)
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}