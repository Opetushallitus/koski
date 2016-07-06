package fi.oph.koski.cache

object KoskiCache {
  def cacheStrategy(name: String) = CachingStrategy.cacheAllRefresh(name, 3600, 100)
}
