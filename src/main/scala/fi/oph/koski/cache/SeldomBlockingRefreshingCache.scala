package fi.oph.koski.cache

import com.google.common.cache.{CacheBuilder, CacheLoader, CacheStats, LoadingCache}
import com.google.common.util.concurrent.{Futures, ListenableFuture, UncheckedExecutionException}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Invocation

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

/**
 * Implementation of refreshing cache using Guava refreshAfterWrite semantics, with background reload.
 *
 * Optimized for use in HealthChecker, where the actual health check result should be fast, even if it means that sometimes
 * old result is returned. The duration parameter is used for hard expiration (Guava cache's expireAfterWrite)
 * and refreshDuration for soft background refresh (Guava cache's refreshAfterWrite)
 */
object SeldomBlockingRefreshingCache {
  case class Params(
    duration: Duration,
    refreshDuration: Duration,
    maxSize: Int,
    executor: ExecutionContextExecutor,
    storeValuePredicate: (Invocation, AnyRef) => Boolean = { case (invocation, value) => true },
  ) extends CacheParams

  def apply(name: String, duration: Duration, refreshDuration: Duration, maxSize: Int, executor: ExecutionContextExecutor)(implicit manager: CacheManager): SeldomBlockingRefreshingCache =
    new SeldomBlockingRefreshingCache(name, Params(duration, refreshDuration, maxSize, executor))
}

class SeldomBlockingRefreshingCache(val name: String, val params: SeldomBlockingRefreshingCache.Params)(implicit manager: CacheManager) extends Cache with Logging {
  private val debugCaching = false

  if (debugCaching) {
    logger.info("Create seldom blocking refreshing cache " + name)
  }
  manager.registerCache(this)
  /**
   *  Marker exception that's used for preventing caching values that we don't want to cache.
   */
  case class DoNotStoreException(value: AnyRef) extends RuntimeException("Don't store this value!")

  def apply(invocation: Invocation): AnyRef = {
    try {
      val newValue = cache.get(invocation)
      if (debugCaching) {
        logger.info(s"$name stored, cache size ${cache.stats()}")
      }
      newValue
    } catch {
      case e: UncheckedExecutionException if e.getCause.isInstanceOf[DoNotStoreException] => e.getCause.asInstanceOf[DoNotStoreException].value
      case DoNotStoreException(value) => value
      case e: Throwable =>
        if (debugCaching) {
          logger.warn(e)(s"$name fetch failed: ${e.getMessage}")
        }
        throw e
    }
  }

  def stats: CacheStats = cache.stats

  override def invalidateCache(): Unit = {
    cache.invalidateAll()
    if (debugCaching) {
      logger.info(s"$name invalidate (cache size ${cache.size})")
    }
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation: Invocation): AnyRef = {
       loadValueForKey(invocation)
      }

      override def reload(invocation: Invocation, oldValue: AnyRef): ListenableFuture[AnyRef] = {
        Futures.submit(() => {
          try {
            loadValueForKey(invocation)
          } catch {
            case e: Exception =>
              // Invalidate the cache entry if an exception was thrown during background refresh,
              // to prevent the cache from returning stale value. Without this, the cache won't let the callers
              // notice errors during refresh until the hard expireAfterWrite duration has passed.
              cache.invalidate(invocation)
              throw e // Still throw the exception
          }
        }, params.executor)
      }

      private def loadValueForKey(invocation: Invocation): AnyRef = {
        val value = invocation.invoke
        if (!params.storeValuePredicate(invocation, value)) {
          throw new DoNotStoreException(value)
        }
        value
      }
    }

    CacheBuilder
      .newBuilder()
      .recordStats()
      .maximumSize(params.maxSize)
      .refreshAfterWrite(params.refreshDuration.length, params.refreshDuration.unit)
      .expireAfterWrite(params.duration.length, params.duration.unit)
      .build(cacheLoader)
  }
}
