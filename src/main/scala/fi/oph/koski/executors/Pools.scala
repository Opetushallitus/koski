package fi.oph.koski.executors

import java.util.concurrent.ThreadPoolExecutor
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
}
