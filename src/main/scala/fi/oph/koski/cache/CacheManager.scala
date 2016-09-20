package fi.oph.koski.cache

// CacheManager tracks caches, allows exposing all to JMX and invalidating all caches
class CacheManager {
  private var _caches: List[Cache] = Nil

  def invalidateCache = synchronized {
    _caches.foreach(_.invalidateCache)
  }

  def registerCache(cache: Cache) = synchronized {
    _caches = cache :: _caches
  }

  def caches = _caches
}

// CacheManager for global caches. Use for global object only.
object GlobalCacheManager extends CacheManager