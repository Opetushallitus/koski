package fi.oph.tor.cache

import fi.oph.tor.util.{Invocation, Proxy}
import fi.vm.sade.utils.memoize.TTLCache
import fi.vm.sade.utils.slf4j.Logging

import scala.reflect.ClassTag

object CachingProxy {
  def apply[S <: AnyRef](strategy: CachingStrategy, service: S)(implicit tag: ClassTag[S]) = {
    Proxy.createProxy[S](service, { invocation =>
      strategy.apply(invocation)
    })
  }
}

trait CachingStrategy extends Function1[Invocation, AnyRef] {
}

object NoCache extends CachingStrategy {
  override def apply(invocation: Invocation) = invocation.invoke
}

abstract class CachingStrategyBase(durationSeconds: Int, maxSize: Int) extends CachingStrategy with Logging {
  def apply(invocation: Invocation): AnyRef

  protected def invokeAndStore(invocation: Invocation) = invokeAndPossiblyStore(invocation)(_ => true)

  protected def invokeAndPossiblyStore(invocation: Invocation)(storeValuePredicate: AnyRef => Boolean) = {
    val key: String = cacheKey(invocation)
    cache.get(key) match {
      case None =>
        //logger.debug("Cache miss: " + key)
        val value = invocation.invoke
        if (storeValuePredicate(value)) {
          //logger.debug("Storing: " + key + "=" + value)
          cache.put(key, value)
        }
        value
      case Some(value) =>
        //logger.debug("Cache hit: " + key + "=" + value)
        value
    }
  }

  private val cache = TTLCache[String, AnyRef](durationSeconds, maxSize)
  private def cacheKey(invocation: Invocation) = invocation.method.toString + invocation.args.mkString(",")
}

case class CacheAll(durationSeconds: Int, maxSize: Int) extends CachingStrategyBase(durationSeconds, maxSize) {
  def apply(invocation: Invocation): AnyRef = invokeAndStore(invocation)
}