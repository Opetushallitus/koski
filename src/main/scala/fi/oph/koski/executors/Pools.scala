package fi.oph.koski.executors

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

object ManagedExecutionContext {
  def fromExecutor(name: String, executor: ThreadPoolExecutor) = {
    ExecutionContext.fromExecutor(ManagedThreadPoolExecutor.register(name, executor))
  }
}