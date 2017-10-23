package fi.oph.koski.cache

import java.util.concurrent.TimeUnit._

import com.google.common.cache.CacheStats
import com.google.common.util.concurrent.MoreExecutors._
import fi.oph.koski.util.{Invocation, Pools}

import scala.concurrent.duration.Duration

object Cache {
  def cacheAllRefresh(name: String, durationSeconds: Int, maxSize: Int)(implicit invalidator: CacheManager): Cache = cacheAllRefresh(name, Duration(durationSeconds, SECONDS), maxSize)
  def cacheAllRefresh(name: String, duration: Duration, maxSize: Int)(implicit invalidator: CacheManager) = new RefreshingCache(name, CacheParamsRefreshing(duration, maxSize), invalidator)
  def cacheAllNoRefresh(name: String, durationSeconds: Int, maxSize: Int)(implicit invalidator: CacheManager): Cache = cacheAllNoRefresh(name, Duration(durationSeconds, SECONDS), maxSize)
  def cacheAllNoRefresh(name: String, duration: Duration, maxSize: Int)(implicit invalidator: CacheManager) = new GuavaCache(name, CacheParamsExpiring(duration, maxSize), invalidator)
  def cacheNoRefresh(name: String, params: CacheParamsExpiring)(implicit invalidator: CacheManager) = new GuavaCache(name, params, invalidator)
  private[cache] val executorService = listeningDecorator(ExecutionContextExecutorServiceBridge(Pools.globalExecutor))
}

trait Cache extends Cached {
  def name: String
  def stats: CacheStats
  def params: CacheParams
  def apply(invocation: Invocation): AnyRef
}

sealed trait CacheParams {
  def duration: Duration
  def maxSize: Int
}
case class CacheParamsExpiring(duration: Duration, maxSize: Int, storeValuePredicate: (Invocation, AnyRef) => Boolean = { case (invocation, value) => true }) extends CacheParams
case class CacheParamsRefreshing(duration: Duration, maxSize: Int, maxExcessRatio: Double = 0.1, refreshScatteringRatio: Double = 0.1) extends CacheParams