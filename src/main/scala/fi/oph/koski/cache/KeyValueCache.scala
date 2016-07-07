package fi.oph.koski.cache

import fi.oph.koski.util.Invocation

// Simple key-value cache: caches the results of a given unary function
case class KeyValueCache[K <: AnyRef, V <: AnyRef](strategy: CachingStrategy, loader: K => V) {
  def apply(key: K): V = {
    strategy.apply(Invocation(loader, key)).asInstanceOf[V]
  }
}

case class SingleValueCache[V <: AnyRef](strategy: CachingStrategy, loader: () => V) {
  def apply: V = {
    strategy.apply(Invocation.apply({ s: String => loader() }, "getSingleValue")).asInstanceOf[V]
  }
}