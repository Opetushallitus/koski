package fi.oph.koski.cache

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import com.google.common.cache.CacheStats

class JMXCacheManager extends CacheManager with JMXCacheManagerMXBean {
  val mbeanServer = ManagementFactory.getPlatformMBeanServer()

  mbeanServer.registerMBean(this, new ObjectName(s"fi.oph.koski:type=Cache,name=CacheManager"))

  override def registerCache(cache: Cache) = {
    mbeanServer.registerMBean(new CacheMBean(cache), new ObjectName(s"fi.oph.koski:type=Cache,name=${cache.name}"))
    super.registerCache(cache)
  }

  def getHitRate = overallStats.hitRate
  def getLoadExceptionCount = overallStats.loadExceptionCount
  def getHitCount = overallStats.hitCount
  def getMissCount = overallStats.missCount
  def getTotalLoadTime = overallStats.totalLoadTime()

  def overallStats: CacheStats = {
    caches.foldLeft(new CacheStats(0, 0, 0, 0, 0, 0)) { case (stats, cache) =>
      stats.plus(cache.stats)
    }
  }
}

trait JMXCacheManagerMXBean {
  def invalidateAllCaches: Unit
  def getHitRate: Double
  def getHitCount: Long
  def getMissCount: Long
  def getLoadExceptionCount: Long
  def getTotalLoadTime: Long
}

class CacheMBean(cache: Cache) extends CacheMBeanMXBean {
  def getRequestCount = cache.stats.requestCount
  def getHitRate = cache.stats.hitRate
  def getTotalLoadTime = cache.stats.totalLoadTime
  def getMissCount = cache.stats.missCount
  def getLoadSuccessCount = cache.stats.loadSuccessCount
  def getMissRate = cache.stats.missRate
  def getLoadExceptionCount = cache.stats.loadExceptionCount
  def getEvictionCount = cache.stats.evictionCount
  def getLoadCount = cache.stats.loadCount
  def getHitCount = cache.stats.hitCount
  def getMaxSize = cache.params.maxSize
  def getCacheDurationSeconds = cache.params.durationSeconds
  def getBackgroundRefresh = cache.params.backgroundRefresh
  def invalidateCache = cache.invalidateCache()
}

trait CacheMBeanMXBean {
  def getRequestCount: Long
  def getHitCount: Long
  def getHitRate: Double
  def getMissCount: Long
  def getMissRate: Double
  def getLoadCount: Long
  def getLoadSuccessCount: Long
  def getLoadExceptionCount: Long
  def getTotalLoadTime: Long
  def getEvictionCount: Long
  def getMaxSize: Int
  def getCacheDurationSeconds: Int
  def getBackgroundRefresh: Boolean
  def invalidateCache: Unit
}