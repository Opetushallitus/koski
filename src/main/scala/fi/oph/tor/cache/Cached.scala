package fi.oph.tor.cache

trait Cached {
  def invalidateCache(): Unit
}
