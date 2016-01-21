package fi.oph.tor.koodisto

import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor}
import fi.oph.tor.cache.{CacheAll, TorCache, CachingProxy}
import fi.vm.sade.utils.Timer
import fi.vm.sade.utils.slf4j.Logging

object KoodistoCacheWarmer extends Logging {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {

    val cacheStrategy: CacheAll = TorCache.cacheStrategy
    val cached = CachingProxy(cacheStrategy, koodistoPalvelu)
    MockKoodistoPalvelu.koodistot.foreach { koodisto =>
      cached.getLatestVersion(koodisto).foreach(cached.getKoodistoKoodit(_))
    }
    cached
  }
}
