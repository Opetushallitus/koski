package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.log.TimedProxy

object KoodistoPalvelu {
  def apply(config: Config) = {
    CachingProxy(TorCache.cacheStrategy, TimedProxy(withoutCache(config)))
  }

  def withoutCache(config: Config): KoodistoPalvelu = {
    if (config.hasPath("koodisto.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("koodisto.virkailija.url"))
    } else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url"))
    } else {
      MockKoodistoPalvelu
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodiMetadata(koodi: KoodistoKoodi): List[KoodistoKoodiMetadata]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}