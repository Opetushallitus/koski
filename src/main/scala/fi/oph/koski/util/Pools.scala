package fi.oph.koski.util

import java.lang.management.ManagementFactory
import java.util.concurrent._
import javax.management.ObjectName

import scala.concurrent.ExecutionContext

/**
 *  We've tried to bundle all global threadpool/executor/executioncontext settings here.
 */
object Pools {
  // Number of threads to use for Executors.global (scala.concurrent, our own GlobalExecution context, parallel collections etc)
  val jettyThreads = 100
  val globalExecutionContextThreads = jettyThreads
  val backgroundExecutionContextThreads = Math.max(jettyThreads / 10, 2)
  val httpThreads = jettyThreads
  val httpPool = NamedThreadPool("http4s-blaze-client", httpThreads)
  val dbThreads = 20
  val globalExecutor = ManagedExecutionContext.fromExecutor("globalPool", new ThreadPoolExecutor(Pools.globalExecutionContextThreads, Pools.globalExecutionContextThreads, 60, TimeUnit.SECONDS, new ArrayBlockingQueue[Runnable](1000)))
  val backgroundExecutor = ManagedExecutionContext.fromExecutor("backgroundPool", new ThreadPoolExecutor(0, Pools.backgroundExecutionContextThreads, 60, TimeUnit.SECONDS, new ArrayBlockingQueue[Runnable](1000)))
}

object NamedThreadPool {
  def apply(name: String, size: Int) = ThreadPoolExecutorMXBean.register(name, Executors.newFixedThreadPool(size, threadFactory(name)).asInstanceOf[ThreadPoolExecutor])

  private def threadFactory(namePrefix: String) = new ThreadFactory {
    val defaultThreadFactory = Executors.defaultThreadFactory()
    private var idCounter = 0
    private def nextId = {
      idCounter = idCounter + 1
      idCounter
    }
    def newThread(r: Runnable) = this.synchronized {
      val t = defaultThreadFactory.newThread(r)
      t.setName(namePrefix + "-thread-" + nextId)
      t.setDaemon(true)
      t
    }
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

class ThreadPoolExecutorMXBean(executor: ThreadPoolExecutor) extends ThreadPoolMXBean {
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

object ThreadPoolExecutorMXBean {
  private val mbeanServer = ManagementFactory.getPlatformMBeanServer()
  def register(name: String, executor: ThreadPoolExecutor) = {
    mbeanServer.registerMBean(new ThreadPoolExecutorMXBean(executor), new ObjectName(s"fi.oph.koski:type=ThreadPool,name=" + name))
    executor
  }
}

object ManagedExecutionContext {
  def fromExecutor(name: String, executor: ThreadPoolExecutor) = {
    ExecutionContext.fromExecutor(ThreadPoolExecutorMXBean.register(name, executor))
  }
}