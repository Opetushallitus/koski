package fi.oph.koski.cache

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.common.util.concurrent.{ListenableFuture, UncheckedExecutionException}
import com.google.common.util.concurrent.MoreExecutors._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{Invocation, Pools}

object Cache {
  def cacheAllRefresh(name: String, durationSeconds: Int, maxSize: Int)(implicit invalidator: CacheManager) = new Cache(name, CacheParams(durationSeconds, maxSize, true), invalidator)
  def cacheAllNoRefresh(name: String, durationSeconds: Int, maxSize: Int)(implicit invalidator: CacheManager) = new Cache(name, CacheParams(durationSeconds, maxSize, false), invalidator)
  def cache(name: String, params: CacheParams)(implicit invalidator: CacheManager) = new Cache(name, params, invalidator)
  private[cache] val executorService = listeningDecorator(ExecutionContextExecutorServiceBridge(Pools.globalExecutor))
}

class Cache protected (val name: String, val params: CacheParams, invalidator: CacheManager) extends Cached with Logging {
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

  def stats = cache.stats

  override def invalidateCache() = {
    cache.invalidateAll
    logger.debug(name + ".invalidate (cache size " + cache.size() + ")")
  }

  private val cache: LoadingCache[Invocation, AnyRef] = {
    val cacheLoader: CacheLoader[Invocation, AnyRef] = new CacheLoader[Invocation, AnyRef] {
      override def load(invocation:  Invocation): AnyRef = {
        logger.debug("->loading")
        val value = invocation.invoke
        if (!params.storeValuePredicate(invocation, value)) {
          throw new DoNotStoreException(value)
        }
        value
      }

      override def reload(invocation: Invocation, oldValue: AnyRef): ListenableFuture[AnyRef] = {
        val future: ListenableFuture[AnyRef] = Cache.executorService.submit(new Callable[AnyRef] {
          override def call(): AnyRef = load(invocation)
        })
        future
      }
    }

    val cacheBuilder = CacheBuilder
      .newBuilder()
      .recordStats()
      .maximumSize(params.maxSize)

    (if(params.backgroundRefresh) {
      cacheBuilder.refreshAfterWrite(params.durationSeconds, SECONDS)
    } else {
      cacheBuilder.expireAfterWrite(params.durationSeconds, SECONDS)
    }).build(cacheLoader)
  }

  private def cacheKey(invocation: Invocation) = invocation.f.name + invocation.args.mkString(",")
}

case class CacheParams(durationSeconds: Int, maxSize: Int, backgroundRefresh: Boolean, storeValuePredicate: (Invocation, AnyRef) => Boolean = { case (invocation, value) => true })