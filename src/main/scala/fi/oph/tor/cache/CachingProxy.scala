package fi.oph.tor.cache

import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.{Callable, Executors, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.common.util.concurrent.MoreExecutors.listeningDecorator
import com.google.common.util.concurrent.{MoreExecutors, JdkFutureAdapters, ListenableFuture, UncheckedExecutionException}
import fi.oph.tor.cache.CachingProxy.executorService
import fi.oph.tor.util.{Invocation, Proxy}
import fi.vm.sade.utils.slf4j.Logging

import scala.reflect.ClassTag

object CachingProxy {
  val executorService = listeningDecorator(newFixedThreadPool(10))

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

case class CacheAll(val durationSeconds: Int, val maxSize: Int) extends CachingStrategyBase(durationSeconds, maxSize, (invocation, value) => true) {
}

abstract class CachingStrategyBase(durationSeconds: Int, maxSize: Int, storeValuePredicate: (Invocation, AnyRef) => Boolean) extends CachingStrategy with Logging {
  /**
   *  Marker exception that's used for preventing caching values that we don't want to cache.
   */
  case class DoNotStoreException(val value: AnyRef) extends RuntimeException("Don't store this value!")

  def apply(invocation: Invocation): AnyRef = {
    try {
      cache.get(invocation)
    } catch {
      case e: UncheckedExecutionException if e.getCause.isInstanceOf[DoNotStoreException] => e.getCause.asInstanceOf[DoNotStoreException].value
      case DoNotStoreException(value) => value
    }
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation:  Invocation): AnyRef = {
        val value = invocation.invoke
        if (!storeValuePredicate(invocation, value)) {
          throw new DoNotStoreException(value)
        }
        value
      }

      override def reload(invocation: Invocation, oldValue: AnyRef): ListenableFuture[AnyRef] = {
        val future: ListenableFuture[AnyRef] = executorService.submit(new Callable[AnyRef] {
          override def call(): AnyRef = load(invocation)
        })
        future
      }
    }

    CacheBuilder
      .newBuilder()
      .recordStats()
      .refreshAfterWrite(durationSeconds, TimeUnit.SECONDS)
      .maximumSize(maxSize)
      .build(cacheLoader)
  }

  private def cacheKey(invocation: Invocation) = invocation.method.toString + invocation.args.mkString(",")
}