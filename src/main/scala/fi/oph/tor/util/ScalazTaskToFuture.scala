package fi.oph.tor.util

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import scalaz.concurrent.Task
import scala.concurrent.{ Promise, Future }

object ScalazTaskToFuture {
  def futureToTask[A](x: => Future[A])(implicit executor: ExecutionContext): Task[A] = {
    import scalaz.Scalaz._

    Task.async {
      register =>
        x.onComplete {
          case Success(v) => register(v.right)
          case Failure(ex) => register(ex.left)
        }
    }
  }

  def taskToFuture[A](x: => Task[A]): Future[A] = {
    import scalaz.{ \/-, -\/ }
    val p: Promise[A] = Promise()
    x.runAsync {
      case -\/(ex) =>
        p.failure(ex); ()
      case \/-(r) => p.success(r); ()
    }
    p.future
  }
}