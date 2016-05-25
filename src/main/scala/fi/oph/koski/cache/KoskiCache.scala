package fi.oph.koski.cache

object KoskiCache {
  def cacheStrategy = CachingStrategy.cacheAllRefresh(3600, 100)
}
