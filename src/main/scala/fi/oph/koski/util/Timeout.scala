package fi.oph.koski.util

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, CancellationException, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object CancellableFuture {
  type CancelFn = () => Boolean

  def apply[T](f: => T)(onTimeout: => Unit)(implicit ec: ExecutionContext): (Future[T], CancelFn) = {
    val promise = Promise[T]()
    val future = promise.future
    val ref = new AtomicReference[Thread](null)

    promise.tryCompleteWith(Future {
      val thread = Thread.currentThread()
      ref.synchronized { ref.set(thread) }
      try f finally {
        val wasInterrupted = ref.synchronized { ref.getAndSet(null) } != thread
        if (wasInterrupted) {
          onTimeout
        }
      }
    })

    (future, () => {
      ref.synchronized {
        Option(ref.getAndSet(null)).foreach(_.interrupt())
        promise.tryFailure(new CancellationException)
      }
    })
  }
}

object Timeout {
  def apply[T](duration: Duration)(f: => T)(implicit ec: ExecutionContext): T = {
    val (future, cancel) = CancellableFuture(f)(() => {})
    try {
      Await.result(future, duration)
    } catch {
      case e: Throwable =>
        cancel()
        throw e
    }
  }
}
