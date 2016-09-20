package fi.oph.koski.cache

class CacheInvalidator extends Cached {
  private var caches: List[Cached] = Nil

  def invalidateCache = synchronized {
    caches.foreach(_.invalidateCache)
  }

  def registerCache(cache: Cached) = synchronized {
    caches = cache :: caches
  }
}

object GlobalCacheInvalidator extends CacheInvalidator