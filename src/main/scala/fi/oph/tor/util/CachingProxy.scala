package fi.oph.tor.util

import fi.vm.sade.utils.memoize.TTLCache

import scala.reflect.ClassTag

object CachingProxy {

  def apply[S <: AnyRef](service: S)(implicit tag: ClassTag[S]) = {
    val cache = TTLCache[String, CacheEntry](60000, 100)

    Proxy.createProxy[S](service, { case (invocation, defaultHandler) =>
      val cacheKey = invocation.method.toString + invocation.args.mkString(",")
      cache.getOrElseUpdate(cacheKey, () => CacheEntry(defaultHandler(invocation))).value
    })
  }

}

case class CacheEntry(value: AnyRef)
