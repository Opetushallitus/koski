package fi.oph.tor.util

import java.io.Serializable
import com.typesafe.config.Config
import fi.oph.tor.util.Caches._
import fi.vm.sade.utils.memoize.{Caching, Cache, TTLCache}

import scala.reflect.ClassTag

object CachingProxy {
  def apply[S <: AnyRef](config: Config, service: S)(implicit tag: ClassTag[S]) = {
    val caches = Caches.apply(config, tag.runtimeClass.getName)
    Proxy.createProxy[S](service, { case (invocation, defaultHandler) =>
      val cacheKey = invocation.method.toString + invocation.args.mkString(",")
      caches.getCache(invocation)(defaultHandler(invocation))
    })
  }
}

trait Caches {
  def getCache(invocation: Invocation): CachingMethod[AnyRef]
}

object Caches {
  type CachingMethod[R] = ((=> R) => R)
  def noCache(fetch: => AnyRef): AnyRef = fetch

  def apply(config: Config, className: String) = {
    val cachePath = "cache." + className
    val cacheConfig = if (config.hasPath(cachePath)) {
      config.getConfig(cachePath)
    } else {
      config.getConfig("cache")
    }
    new Caches {
      val classSpecificCache = TTLCache[String, CacheEntry](cacheConfig.getInt("durationSeconds"), cacheConfig.getInt("maxSize"))
      override def getCache(invocation: Invocation) = {
        def actuallyCache(fetch: => AnyRef): AnyRef = {
          val cacheKey = invocation.method.toString + invocation.args.mkString(",")
          classSpecificCache.getOrElseUpdate(cacheKey, () => CacheEntry(fetch)).value
        }

        val methodConfigKey = invocation.method.getName
        val methodConfig: String = cacheConfig.getOptionalString(methodConfigKey).getOrElse(cacheConfig.getOptionalString("_").getOrElse("cache"))
        methodConfig match {
          case "no-cache" =>
            noCache _
          case "cache" =>
            actuallyCache _
        }
      }
    }
  }

  implicit class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }
  }
}
case class CacheConfig(ttl: Long, maxSize: Int)

case class CacheEntry(value: AnyRef)