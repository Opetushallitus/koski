package fi.oph.tor.koodisto

import fi.oph.tor.cache.{CacheAllRefresh, CachingProxy, TorCache}
import fi.oph.tor.log.Logging

object KoodistoCacheWarmer extends Logging {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {

    val cacheStrategy: CacheAllRefresh = TorCache.cacheStrategy
    val cached = CachingProxy(cacheStrategy, koodistoPalvelu)
    MockKoodistoPalvelu.koodistot.foreach { koodisto =>
      cached.getLatestVersion(koodisto).foreach(cached.getKoodistoKoodit(_))
    }
    cached
  }
}
