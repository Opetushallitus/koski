package fi.oph.tor.cache

import java.util.concurrent.{Callable, TimeUnit}

import com.google.common.cache.{Cache, CacheBuilder}
import fi.oph.tor.util.{Invocation, Proxy}
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

object CachingStrategy {
  def noCache = NoCache
  def cacheAll(durationSeconds: Int, maxSize: Int) = CacheAll(durationSeconds, maxSize)
}

object NoCache extends CachingStrategy {
  override def apply(invocation: Invocation) = invocation.invoke
}

case class CacheAll(val durationSeconds: Int, val maxSize: Int) extends CachingStrategyBase(durationSeconds, maxSize) {
  def apply(invocation: Invocation): AnyRef = invokeAndStore(invocation)
}

abstract class CachingStrategyBase(durationSeconds: Int, maxSize: Int) extends CachingStrategy with Logging {
  /**
   *  Marker exception that's used for preventing caching values that we don't want to cache.
   */
  case class DoNotStoreException(val value: AnyRef) extends RuntimeException("Don't store this value!")

  def apply(invocation: Invocation): AnyRef

  protected def invokeAndStore(invocation: Invocation) = invokeAndPossiblyStore(invocation)(_ => true)

  protected def invokeAndPossiblyStore(invocation: Invocation)(storeValuePredicate: AnyRef => Boolean) = this.synchronized {
    val key: String = cacheKey(invocation)
    try {
      cache.get(key, new Callable[AnyRef] {
        def call() = {
          val value = invocation.invoke
          if (!storeValuePredicate(value)) {
            throw new DoNotStoreException(value)
          }
          value
        }
      })
    } catch {
      case DoNotStoreException(value) => value
    }
  }

  private val cache: Cache[String, AnyRef] = CacheBuilder
    .newBuilder()
    .recordStats()
    .expireAfterWrite(durationSeconds, TimeUnit.SECONDS)
    .maximumSize(maxSize)
    .build().asInstanceOf[Cache[String, AnyRef]]

  private def cacheKey(invocation: Invocation) = invocation.method.toString + invocation.args.mkString(",")
}