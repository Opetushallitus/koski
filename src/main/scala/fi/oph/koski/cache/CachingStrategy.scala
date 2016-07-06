package fi.oph.koski.cache

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.common.util.concurrent.MoreExecutors._
import com.google.common.util.concurrent.{ListenableFuture, UncheckedExecutionException}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{Invocation, Pools}

class CacheInvalidator extends Cached {
  private var caches: List[Cached] = Nil

  def invalidateCache = synchronized {
    caches.foreach(_.invalidateCache)
  }

  def registerCache(cache: Cached) = synchronized {
    caches = cache :: caches
  }
}

object GlobalCacheInvalidator extends CacheInvalidator

object CachingStrategy {
  def cacheAllRefresh(name: String, durationSeconds: Int, maxSize: Int, invalidator: CacheInvalidator = GlobalCacheInvalidator) = CachingStrategy(name, CacheAllCacheDetails(durationSeconds, maxSize, true), invalidator)
  def cacheAllNoRefresh(name: String, durationSeconds: Int, maxSize: Int, invalidator: CacheInvalidator = GlobalCacheInvalidator) = CachingStrategy(name, CacheAllCacheDetails(durationSeconds, maxSize, false), invalidator)
  private[cache] val executorService = listeningDecorator(Pools.globalPool)
}

case class CachingStrategy(name: String, cacheDetails: CacheDetails, invalidator: CacheInvalidator = GlobalCacheInvalidator) extends Cached with Logging {
  logger.debug("Create cache " + name)
  invalidator.registerCache(this)
  /**
   *  Marker exception that's used for preventing caching values that we don't want to cache.
   */
  case class DoNotStoreException(value: AnyRef) extends RuntimeException("Don't store this value!")

  def apply(invocation: Invocation): AnyRef = {
    try {
      logger.debug(name + "." + invocation + " (cache size " + cache.size() + ")")
      cache.get(invocation)
    } catch {
      case e: UncheckedExecutionException if e.getCause.isInstanceOf[DoNotStoreException] => e.getCause.asInstanceOf[DoNotStoreException].value
      case DoNotStoreException(value) => value
    }
  }

  override def invalidateCache() = {
    cache.invalidateAll
    logger.debug(name + ".invalidate (cache size " + cache.size() + ")")
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation:  Invocation): AnyRef = {
        logger.debug("->loading")
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

case class CacheAllCacheDetails(durationSeconds: Int, maxSize: Int, refreshing: Boolean) extends CacheDetails {
  override def storeValuePredicate: (Invocation, AnyRef) => Boolean = (invocation, value) => true
}
