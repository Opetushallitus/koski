package fi.oph.tor.koodisto

import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor}
import fi.oph.tor.cache.{CacheAll, TorCache, CachingProxy}
import fi.vm.sade.utils.Timer
import fi.vm.sade.utils.slf4j.Logging

object KoodistoCacheWarmer extends Logging {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {

    val cacheStrategy: CacheAll = TorCache.cacheStrategy
    val cached = CachingProxy(cacheStrategy, koodistoPalvelu)

      new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(new Runnable {
        def run(): Unit = {
          Timer.timed("Warming up koodisto caches") {
            MockKoodistoPalvelu.koodistot.foreach { koodisto =>
              cached.getLatestVersion(koodisto).foreach(cached.getKoodistoKoodit(_))
            }
          }
        }
      }, 0, cacheStrategy.durationSeconds, TimeUnit.SECONDS)

    cached
  }
}
