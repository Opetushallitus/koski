package fi.oph.koski.cache

import com.google.common.cache.{CacheBuilder, CacheLoader, CacheStats, LoadingCache}
import com.google.common.util.concurrent.UncheckedExecutionException
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Invocation

import java.util.UUID
import scala.concurrent.duration.Duration

object ExpiringCache {
  case class Params(duration: Duration, maxSize: Int, storeValuePredicate: (Invocation, AnyRef) => Boolean = { case (invocation, value) => true }) extends CacheParams

  def apply(name: String, duration: Duration, maxSize: Int)(implicit manager: CacheManager): ExpiringCache = new ExpiringCache(name, Params(duration, maxSize))
}

class ExpiringCache(val name: String, val params: ExpiringCache.Params)(implicit manager: CacheManager) extends Cache with Logging {
  private val debugCaching = false

  if (debugCaching) {
    logger.info("Create expiring cache " + name)
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

  override def invalidateCache() = {
    cache.invalidateAll
    if (debugCaching) {
      logger.info(s"$name invalidate (cache size ${cache.size})")
    }
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation:  Invocation): AnyRef = {
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
      .expireAfterWrite(params.duration.length, params.duration.unit)
      .build(cacheLoader)
  }
}
