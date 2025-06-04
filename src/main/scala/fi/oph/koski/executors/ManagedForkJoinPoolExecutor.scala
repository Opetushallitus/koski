package fi.oph.koski.executors

import java.lang.management.ManagementFactory
import java.util.concurrent.ForkJoinPool
import javax.management.ObjectName

class ManagedForkJoinPoolExecutor(executor: ForkJoinPool) extends ManagedForkJoinPoolMXBean {
  def getPoolSize: Int = executor.getPoolSize
  def getActiveThreadCount: Int = executor.getActiveThreadCount
  def getRunningThreadCount: Int = executor.getRunningThreadCount
  def getQueuedSubmissionCount: Int = executor.getQueuedSubmissionCount
  def getQueuedTaskCount: Long = executor.getQueuedTaskCount
  def isQuiescent: Boolean = executor.isQuiescent
  def getParallelism: Int = executor.getParallelism
}

object ManagedForkJoinPoolExecutor {
  private val mbeanServer = ManagementFactory.getPlatformMBeanServer()

  def register(name: String, executor: ForkJoinPool): ForkJoinPool = {
    mbeanServer.registerMBean(new ManagedForkJoinPoolExecutor(executor), new ObjectName(s"fi.oph.koski:type=ForkJoinPool,name=$name"))
    executor
  }
}

trait ManagedForkJoinPoolMXBean {
  def getPoolSize: Int
  def getActiveThreadCount: Int
  def getRunningThreadCount: Int
  def getQueuedSubmissionCount: Int
  def getQueuedTaskCount: Long
  def isQuiescent: Boolean
  def getParallelism: Int
}
