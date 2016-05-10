package fi.oph.tor.cache

import fi.oph.tor.util.Invocation

// Simple key-value cache: caches the results of a given unary function
case class KeyValueCache[K <: AnyRef, V <: AnyRef](strategy: CachingStrategy, loader: K => V) {
  def apply(key: K): V = {
    strategy.apply(Invocation(loader, key)).asInstanceOf[V]
  }
}
