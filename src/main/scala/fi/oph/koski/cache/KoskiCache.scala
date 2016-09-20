package fi.oph.koski.cache

object KoskiCache {
  def cacheStrategy(name: String) = Cache.cacheAllRefresh(name, 3600, 100)
}
