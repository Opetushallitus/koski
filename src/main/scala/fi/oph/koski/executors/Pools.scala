package fi.oph.koski.executors

import java.util.concurrent.{ExecutorService, Executors, ForkJoinPool, ThreadPoolExecutor}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
 *  We've tried to bundle all global threadpool/executor/executioncontext settings here.
 */
object Pools {
  // Number of threads to use for Executors.global (scala.concurrent, our own GlobalExecution context, parallel collections etc)
  val jettyThreads = 250
  val globalExecutionContextThreads: Int = jettyThreads
  val httpThreads: Int = jettyThreads
  val databasePoolName = "databasePool"

  val globalPoolExecutor: ThreadPoolExecutor = NamedThreadPoolExecutor("globalPool", Pools.globalExecutionContextThreads, Pools.globalExecutionContextThreads, 1000)
  val globalExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(
    globalPoolExecutor
  )
  val databaseExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(
    NamedThreadPoolExecutor(databasePoolName, Pools.globalExecutionContextThreads, Pools.globalExecutionContextThreads, 1000)
  )

  // Own pools for the cats effect IO used by http4s. This avoids deadlocks, when http requests cannot get deadlocked when the global pool is full of threads
  // waiting for the http requests to complete.
  val httpComputeExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(NamedForkJoinPoolExecutor("httpComputePool"))
  val httpBlockingExecutor: ExecutionContextExecutor = ExecutionContext.fromExecutor(NamedThreadPoolExecutor("httpBlockingPool", Pools.httpThreads, Pools.httpThreads, 1000))
}
