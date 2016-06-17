package fi.oph.koski.util

import java.util.concurrent.{Executors, ThreadFactory}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.math._

/**
 *  We've tried to bundle all global threadpool/executor/executioncontext settings here.
 */
object Pools {
  // Number of threads to use for Executors.global (scala.concurrent, our own GlobalExecution context, parallel collections etc)
  val globalExecutionContextThreads = max(8, (Runtime.getRuntime.availableProcessors * 4))
  val jettyThreads = 20
  val forkJoinThreads = 10
  val httpThreads = max(4, (Runtime.getRuntime.availableProcessors * 1.5).ceil.toInt)
  val httpPool = NamedThreadPool("http4s-blaze-client", httpThreads)
  // If this is less than jettyThreads, weird problems seem to arise (db pool connections get stuck)
  val dbThreads = jettyThreads
  val globalPool = ExecutionContextExecutorServiceBridge(ExecutionContext.global)

  System.setProperty("scala.concurrent.context.minThreads", globalExecutionContextThreads.toString)
  System.setProperty("scala.concurrent.context.maxThreads", globalExecutionContextThreads.toString)

  def init(): Unit = {
    // Makes sure the system props are set
  }
}

object NamedThreadPool {
  def apply(name: String, size: Int) = Executors.newFixedThreadPool(size, threadFactory(name))

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

import java.util.Collections
import java.util.concurrent.{AbstractExecutorService, TimeUnit}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object ExecutionContextExecutorServiceBridge {
  def apply(ec: ExecutionContextExecutor): ExecutionContextExecutorService = ec match {
    case eces: ExecutionContextExecutorService => eces
    case _ => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = ec
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown() = ()
      override def shutdownNow() = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = ec execute runnable
      override def reportFailure(t: Throwable): Unit = ec reportFailure t
      override def awaitTermination(length: Long,unit: TimeUnit): Boolean = false
    }
  }
}