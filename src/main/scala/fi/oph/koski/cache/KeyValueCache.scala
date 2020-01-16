package fi.oph.koski.cache

import fi.oph.koski.util.Invocation

// Simple key-value cache: caches the results of a given unary function
case class KeyValueCache[K <: AnyRef, V <: AnyRef](strategy: Cache, loader: K => V) {
  def apply(key: K): V = {
    strategy.apply(Invocation(loader, key)).asInstanceOf[V]
  }
}

case class SingleValueCache[V <: AnyRef](strategy: Cache, loader: () => V) {
  def apply: V = {
    strategy.apply(Invocation.apply(loader)).asInstanceOf[V]
  }
}
