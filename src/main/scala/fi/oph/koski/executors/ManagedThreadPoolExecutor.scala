package fi.oph.koski.executors

import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadPoolExecutor
import javax.management.ObjectName

class ManagedThreadPoolExecutor(executor: ThreadPoolExecutor) extends ThreadPoolMXBean {
  def getActiveCount = executor.getActiveCount
  def getCompletedTaskCount = executor.getCompletedTaskCount
  def getCorePoolSize = executor.getCorePoolSize
  def getLargestPoolSize = executor.getLargestPoolSize
  def getMaximumPoolSize = executor.getMaximumPoolSize
  def getPoolSize = executor.getPoolSize
  def getTaskCount = executor.getTaskCount
  def getQueuedTaskCount = executor.getTaskCount - getCompletedTaskCount - getActiveCount
  def getIdleCount = getPoolSize - getActiveCount
}

object ManagedThreadPoolExecutor {
  private val mbeanServer = ManagementFactory.getPlatformMBeanServer()
  def register(name: String, executor: ThreadPoolExecutor) = {
    mbeanServer.registerMBean(new ManagedThreadPoolExecutor(executor), new ObjectName(s"fi.oph.koski:type=ThreadPool,name=$name"))
    executor
  }
}

trait ThreadPoolMXBean {
  def getActiveCount: Int
  def getCompletedTaskCount: Long
  def getCorePoolSize: Int
  def getLargestPoolSize: Int
  def getMaximumPoolSize: Int
  def getPoolSize: Int
  def getTaskCount: Long
  def getQueuedTaskCount: Long
  def getIdleCount: Int
}