package fi.oph.koski.cache

import com.google.common.cache.CacheStats
import com.google.common.util.concurrent.MoreExecutors._
import fi.oph.koski.util.{Invocation, Pools}

import scala.concurrent.duration.Duration

object Cache {
  private[cache] val executorService = listeningDecorator(ExecutionContextExecutorServiceBridge(Pools.globalExecutor))
}

trait Cache extends Cached {
  def name: String
  def stats: CacheStats
  def params: CacheParams
  def apply(invocation: Invocation): AnyRef
}

trait CacheParams {
  def duration: Duration
  def maxSize: Int
}