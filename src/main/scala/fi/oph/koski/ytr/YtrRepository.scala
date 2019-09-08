package fi.oph.koski.ytr

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.HenkilönTunnisteet

import scala.concurrent.duration._

class YtrRepository(client: YtrClient)(implicit cacheInvalidator: CacheManager) {
  private val oidCache: KeyValueCache[YtrCacheKey, Option[YtrOppija]] =
    KeyValueCache(new ExpiringCache("YtrRepository", ExpiringCache.Params(1.hour, maxSize = 1000, storeValuePredicate = {
      case (_, value) => value != None // Don't cache None results
    })), findByCacheKey)

  def find(henkilö: HenkilönTunnisteet): Option[YtrOppija] = {
    oidCache(YtrCacheKey(henkilö.hetu.toList ++ henkilö.vanhatHetut))
  }

  private def findByCacheKey(key: YtrCacheKey): Option[YtrOppija] =
    key.hetut.flatMap(client.oppijaByHetu).headOption
}
