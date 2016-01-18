package fi.oph.tor.cache

object TorCache {
  def cacheStrategy = CachingStrategy.cacheAll(3600, 100)
}
