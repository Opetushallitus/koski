package fi.oph.koski.cache

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.{MILLISECONDS, _}
import collection.mutable.{Map => MutableMap}
import concurrent.duration._

import com.google.common.cache.AbstractCache.SimpleStatsCounter
import com.google.common.cache.CacheStats
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.log.Logging
import fi.oph.koski.util.{Futures, Invocation}

import scala.concurrent.Future

class RefreshingCache(val name: String, val params: CacheParamsRefreshing, invalidator: CacheManager) extends Cache with Logging with GlobalExecutionContext {
  private val statsCounter = new SimpleStatsCounter()
  private val maxExcess = (params.maxSize * params.maxExcessRatio).toInt
  private val entries: MutableMap[Invocation, CacheEntry] = MutableMap.empty
  logger.debug("Create refreshing cache " + name)
  invalidator.registerCache(this)

  override def stats: CacheStats = statsCounter.snapshot()

  override def apply(invocation: Invocation): AnyRef = Futures.await(callAsync(invocation), 1 day)

  def callAsync(invocation: Invocation): Future[AnyRef] = synchronized {
    val current = entries.getOrElseUpdate(invocation, {
      val newEntry = new CacheEntry(invocation)
      cleanup
      newEntry
    })

    current.valueFuture
  }

  override def invalidateCache(): Unit = synchronized {
    entries.values.foreach(_.evict)
    entries.clear
  }

  protected[cache] def getEntry(invocation: Invocation) = synchronized(entries.get(invocation))

  private def cleanup = {
    val diff = entries.size - params.maxSize
    if (diff > maxExcess) {
      entries.values.toList.sortBy(_.lastReadTimestamp).take(diff).foreach { entry =>
        entry.evict
        entries.remove(entry.invocation)
      }
    }
  }

  class CacheEntry(protected[cache] val invocation: Invocation) {
    private var lastRead: Long = System.currentTimeMillis
    private var scheduledRefreshTime: Option[Long] = None
    private var currentValue: Option[Future[AnyRef]] = None
    private var cancelled = false
    private var fetcher = newFetcher

    scheduleRefresh

    def valueFuture = synchronized {
      lastRead = System.currentTimeMillis
      currentValue match {
        case Some(value) =>
          //logger.info("HIT  " + name + " - " + invocation.toString)
          statsCounter.recordHits(1)
          value
        case None =>
          //logger.info("MISS " + name + " - " + invocation.toString)
          statsCounter.recordMisses(1)
          fetcher
      }
    }

    def lastReadTimestamp = synchronized(lastRead)

    def evict = synchronized {
      cancelled = true
      statsCounter.recordEviction
    }

    def getScheduledRefreshTime = synchronized(scheduledRefreshTime)

    private def newFetcher: Future[AnyRef] = {
      val start = System.nanoTime()
      Future {
        try {
          val newValue = invocation.invoke
          statsCounter.recordLoadSuccess(System.nanoTime() - start)
          CacheEntry.this.synchronized {
            currentValue = Some(fetcher)
          }
          newValue
        } catch {
          case e: Exception =>
            statsCounter.recordLoadException(System.nanoTime() - start)
            CacheEntry.this.synchronized {
              currentValue = currentValue.orElse(Some(fetcher))
            }
            throw e
        } finally {
          scheduleRefresh
        }
      }
    }

    private def scheduleRefresh = synchronized {
      if (!scheduledRefreshTime.isDefined) {
        val variation = params.refreshScatteringRatio // add some random variation to refresh time
        val randomizedFactor: Double = Math.random() * variation + (1.0 - variation)
        val delayMillis = (params.duration.toMillis * randomizedFactor).toLong
        scheduledRefreshTime = Some(System.currentTimeMillis() + delayMillis)
        RefreshingCache.refreshExecutor.schedule(new Runnable { override def run(): Unit = startScheduledRefresh }, delayMillis, MILLISECONDS)
      }
    }

    private def startScheduledRefresh = synchronized {
      scheduledRefreshTime = None
      if (currentValue.isDefined && fetcher.isCompleted && !cancelled) {
        fetcher = newFetcher
      }
    }
  }
}

object RefreshingCache {
  protected[cache] val refreshExecutor = Executors.newSingleThreadScheduledExecutor
}