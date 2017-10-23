package fi.oph.koski.cache

import java.util.concurrent.Callable

import com.google.common.cache.{CacheBuilder, CacheLoader, CacheStats, LoadingCache}
import com.google.common.util.concurrent.{ListenableFuture, UncheckedExecutionException}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Invocation

class GuavaCache (val name: String, val params: CacheParamsExpiring, invalidator: CacheManager) extends Cache with Logging {
  logger.debug("Create guava cache " + name)
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

  def stats: CacheStats = cache.stats

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

    CacheBuilder
      .newBuilder()
      .recordStats()
      .maximumSize(params.maxSize)
      .expireAfterWrite(params.duration.length, params.duration.unit)
      .build(cacheLoader)
  }

  private def cacheKey(invocation: Invocation) = invocation.f.name + invocation.args.mkString(",")
}
