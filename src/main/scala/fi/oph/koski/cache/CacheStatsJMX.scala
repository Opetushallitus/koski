package fi.oph.koski.cache

import java.lang.management.ManagementFactory
import javax.management.ObjectName

class CacheStatsJMX(cacheInvalidator: CacheInvalidator) {
  val server = ManagementFactory.getPlatformMBeanServer()
  cacheInvalidator.caches.foreach { cache =>
    val beanName = new ObjectName(s"fi.oph.koski:type=Cache,name=${cache.name}")
    server.registerMBean(new CacheMBean(cache), beanName)
  }
}

class CacheMBean(cache: Cache) extends CacheMBeanMXBean {
  override def getRequestCount = cache.stats.requestCount
  override def getHitRate = cache.stats.hitRate
  override def getTotalLoadTime = cache.stats.totalLoadTime
  override def getMissCount = cache.stats.missCount
  override def getLoadSuccessCount = cache.stats.loadSuccessCount
  override def getMissRate = cache.stats.missRate
  override def getLoadExceptionCount = cache.stats.loadExceptionCount
  override def getEvictionCount = cache.stats.evictionCount
  override def getLoadCount = cache.stats.loadCount
  override def getHitCount = cache.stats.hitCount
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
}