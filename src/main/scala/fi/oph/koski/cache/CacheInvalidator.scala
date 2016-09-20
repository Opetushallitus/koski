package fi.oph.koski.cache

class CacheInvalidator {
  private var _caches: List[Cache] = Nil

  def invalidateCache = synchronized {
    _caches.foreach(_.invalidateCache)
  }

  def registerCache(cache: Cache) = synchronized {
    _caches = cache :: _caches
  }

  def caches = _caches
}

object GlobalCacheInvalidator extends CacheInvalidator