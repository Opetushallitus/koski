package fi.oph.tor.cache

object TorCache {
  def cacheStrategy = new CacheAll(3600, 100)
}
