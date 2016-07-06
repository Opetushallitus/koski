package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.cache.{CachingProxy, KoskiCache}
import fi.oph.koski.log.TimedProxy

object KoodistoPalvelu {
  def apply(config: Config) = {
    CachingProxy(KoskiCache.cacheStrategy("KoodistoPalvelu"), TimedProxy(withoutCache(config)))
  }

  def withoutCache(config: Config): KoodistoPalvelu = {
    if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url"))
    } else {
      MockKoodistoPalvelu
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}