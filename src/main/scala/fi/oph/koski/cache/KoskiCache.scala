package fi.oph.koski.cache

object KoskiCache {
  def cacheStrategy(name: String)(implicit cacheInvalidator: CacheManager) = Cache.cacheAllRefresh(name, 3600, 100)
}
