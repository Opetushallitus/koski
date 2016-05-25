package fi.oph.koski.cache

trait Cached {
  def invalidateCache(): Unit
}
