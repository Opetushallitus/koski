package fi.oph.koski.ytr

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.henkilo.HenkilönTunnisteet

import scala.concurrent.duration.DurationInt

class YtrRepository(client: YtrClient)(implicit cacheInvalidator: CacheManager) {
  private val cache: KeyValueCache[YtrCacheKey, Option[YtrOppija]] =
    KeyValueCache(new ExpiringCache("YtrRepository", ExpiringCache.Params(5.minute, maxSize = 1000, storeValuePredicate = {
      case (_, value) => value != None // Don't cache None results
    })), uncachedFind)

  def findByTunnisteet(henkilö: HenkilönTunnisteet): Option[YtrOppija] =
    findByCacheKey(YtrCacheKey(henkilö.hetu.toList ++ henkilö.vanhatHetut))

  def findByHetu(hetu: String): Option[YtrOppija] =
    findByCacheKey(YtrCacheKey(List(hetu)))

  def findByCacheKey(key: YtrCacheKey): Option[YtrOppija] =
    cache(key)

  private def uncachedFind(key: YtrCacheKey): Option[YtrOppija] =
    key.hetut.map(hetu => YtrSsnWithPreviousSsns(hetu)).flatMap(client.oppijaByHetu).headOption
}
