package fi.oph.koski.cache

import fi.oph.koski.log.Logging

// CacheManager tracks caches, allows exposing all to JMX and invalidating all caches
class CacheManager extends Logging {
  private var _caches: List[Cache] = Nil

  def invalidateAllCaches = synchronized {
    logger.info("Invalidating all caches")
    _caches.foreach(_.invalidateCache)
  }

  def registerCache(cache: Cache) = synchronized {
    _caches = cache :: _caches
  }

  def caches = _caches
}

// CacheManager for global caches. Use for global object only.
object GlobalCacheManager extends CacheManager