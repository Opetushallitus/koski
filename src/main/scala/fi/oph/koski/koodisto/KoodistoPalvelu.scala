package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.cache.{CacheManager, CachingProxy, RefreshingCache}
import scala.concurrent.duration._

object KoodistoPalvelu {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager) = {
    cached(withoutCache(config))
  }

  def cached(palvelu: KoodistoPalvelu)(implicit cacheInvalidator: CacheManager) = CachingProxy(RefreshingCache("KoodistoPalvelu", 1.hour, 100), palvelu)

  def withoutCache(config: Config): KoodistoPalvelu = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" => MockKoodistoPalvelu()
      case url => new RemoteKoodistoPalvelu(url)
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): List[KoodistoKoodi]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersionRequired(koodistoUri: String): KoodistoViite = {
    getLatestVersionOptional(koodistoUri).getOrElse(throw new RuntimeException(s"Koodistoa ei löydy: $koodistoUri"))
  }
  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite]
}
