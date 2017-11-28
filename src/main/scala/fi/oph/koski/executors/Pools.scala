package fi.oph.koski.executors

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
  val httpPool = NamedThreadPoolExecutor("http4s-blaze-client", httpThreads, httpThreads, 1000)
  val dbThreads = 20
  val databasePoolName = "databasePool"
  val globalExecutor = ExecutionContext.fromExecutor(NamedThreadPoolExecutor("globalPool", Pools.globalExecutionContextThreads, Pools.globalExecutionContextThreads, 1000))
  val databaseExecutor = ExecutionContext.fromExecutor(NamedThreadPoolExecutor(databasePoolName, Pools.globalExecutionContextThreads, Pools.globalExecutionContextThreads, 1000))
  val backgroundExecutor = ExecutionContext.fromExecutor(NamedThreadPoolExecutor("backgroundPool", 0, Pools.backgroundExecutionContextThreads,1000))
}