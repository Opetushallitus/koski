package fi.oph.koski.cache

import fi.oph.koski.log.Logging

// CacheManager tracks caches, allows exposing all to JMX and invalidating all caches
class CacheManager extends Logging {
  private var _caches: List[Cache] = Nil

  def invalidateAllCaches(): Unit = synchronized {
    logger.info("Invalidating all caches")
    _caches.foreach(_.invalidateCache())
  }

  def registerCache(cache: Cache): Unit = synchronized {
    _caches = cache :: _caches
  }

  def caches: List[Cache] = _caches
}

// CacheManager for global caches. Use for global object only.
object GlobalCacheManager extends CacheManager {
  implicit def cacheManager: CacheManager = this
}
