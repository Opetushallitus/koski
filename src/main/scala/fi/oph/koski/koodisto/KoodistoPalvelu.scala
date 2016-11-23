package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, CachingProxy}
import fi.oph.koski.log.TimedProxy

object KoodistoPalvelu {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager) = {
    cached(TimedProxy(withoutCache(config)))
  }

  def cached(palvelu: KoodistoPalvelu)(implicit cacheInvalidator: CacheManager) = CachingProxy(Cache.cacheAllRefresh("KoodistoPalvelu", 3600, 100), palvelu)

  def withoutCache(config: Config): KoodistoPalvelu = {
    if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url"))
    } else {
      MockKoodistoPalvelu()
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}