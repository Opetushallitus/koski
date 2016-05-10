package fi.oph.tor.cache

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.common.util.concurrent.MoreExecutors._
import com.google.common.util.concurrent.{ListenableFuture, UncheckedExecutionException}
import fi.oph.tor.log.Logging
import fi.oph.tor.util.{Invocation, Pools}


trait CachingStrategy extends Function1[Invocation, AnyRef] with Cached

object CachingStrategy {
  def noCache = NoCache
  def cacheAllRefresh(durationSeconds: Int, maxSize: Int) = CacheAllRefresh(durationSeconds, maxSize)
  def cacheAllNoRefresh(durationSeconds: Int, maxSize: Int) = CacheAllNoRefresh(durationSeconds, maxSize)
  private[cache] val executorService = listeningDecorator(Pools.globalPool)
}

object NoCache extends CachingStrategy {
  override def apply(invocation: Invocation) = invocation.invoke

  override def invalidateCache() = {}
}

case class CacheAllRefresh(durationSeconds: Int, maxSize: Int) extends CachingStrategyBase(CacheAllCacheDetails(durationSeconds, maxSize, true))
case class CacheAllNoRefresh(durationSeconds: Int, maxSize: Int) extends CachingStrategyBase(CacheAllCacheDetails(durationSeconds, maxSize, false))

abstract class CachingStrategyBase(cacheDetails: CacheDetails) extends CachingStrategy with Logging {
  /**
   *  Marker exception that's used for preventing caching values that we don't want to cache.
   */
  case class DoNotStoreException(value: AnyRef) extends RuntimeException("Don't store this value!")

  def apply(invocation: Invocation): AnyRef = {
    try {
      cache.get(invocation)
    } catch {
      case e: UncheckedExecutionException if e.getCause.isInstanceOf[DoNotStoreException] => e.getCause.asInstanceOf[DoNotStoreException].value
      case DoNotStoreException(value) => value
    }
  }

  override def invalidateCache() = {
    cache.invalidateAll
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation:  Invocation): AnyRef = {
        val value = invocation.invoke
        if (!cacheDetails.storeValuePredicate(invocation, value)) {
          throw new DoNotStoreException(value)
        }
        value
      }

      override def reload(invocation: Invocation, oldValue: AnyRef): ListenableFuture[AnyRef] = {
        val future: ListenableFuture[AnyRef] = CachingStrategy.executorService.submit(new Callable[AnyRef] {
          override def call(): AnyRef = load(invocation)
        })
        future
      }
    }

    val cacheBuilder = CacheBuilder
      .newBuilder()
      .recordStats()
      .maximumSize(cacheDetails.maxSize)

    (if(cacheDetails.refreshing) {
      cacheBuilder.refreshAfterWrite(cacheDetails.durationSeconds, SECONDS)
    } else {
      cacheBuilder.expireAfterWrite(cacheDetails.durationSeconds, SECONDS)
    }).build(cacheLoader)
  }

  private def cacheKey(invocation: Invocation) = invocation.f.name + invocation.args.mkString(",")
}

trait CacheDetails {
  def durationSeconds: Int
  def maxSize: Int
  def storeValuePredicate: (Invocation, AnyRef) => Boolean
  def refreshing: Boolean
}

abstract case class BaseCacheDetails(durationSeconds: Int, maxSize: Int, refreshing: Boolean) extends CacheDetails

case class CacheAllCacheDetails(durationSeconds: Int, maxSize: Int, refreshing: Boolean) extends CacheDetails {
  override def storeValuePredicate: (Invocation, AnyRef) => Boolean = (invocation, value) => true
}
