package fi.oph.tor.cache

import fi.oph.tor.util.Proxy
import fi.oph.tor.util.Proxy.ProxyHandler

import scala.reflect.ClassTag

// Caching proxy for any interface/trait
object CachingProxy {
  def apply[S <: AnyRef](strategy: CachingStrategy, service: S)(implicit tag: ClassTag[S]): S with Cached = {
    val interfaces: Map[Class[_], (AnyRef, ProxyHandler)] = Map(
      tag.runtimeClass.asInstanceOf[Class[S]] -> (service, { invocation => strategy.apply(invocation)}),
      classOf[Cached] -> (strategy, { invocation => invocation.invoke })
    )
    Proxy.createMultiProxy(interfaces).asInstanceOf[S with Cached]
  }
}