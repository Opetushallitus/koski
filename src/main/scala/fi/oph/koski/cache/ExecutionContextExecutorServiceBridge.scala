package fi.oph.koski.cache

import java.util.Collections
import java.util.concurrent.{AbstractExecutorService, TimeUnit}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, ExecutionContextExecutorService}

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
