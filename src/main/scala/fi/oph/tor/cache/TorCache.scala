package fi.oph.tor.cache

object TorCache {
  def cacheStrategy = CachingStrategy.cacheAllRefresh(3600, 100)
}
