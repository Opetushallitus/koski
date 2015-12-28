package fi.oph.tor.opiskeluoikeus

import rx.lang.scala.{Observable, Subject}
import slick.backend.DatabasePublisher

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait StreamExtensions {
  implicit def extendDatabasePublisher[T](publisher: DatabasePublisher[T]): StreamExtensions.DatabasePublisherExtensions[T] =
    StreamExtensions.DatabasePublisherExtensions[T](publisher)
}

object StreamExtensions {
  implicit class DatabasePublisherExtensions[T](val publisher: DatabasePublisher[T]) extends AnyVal {
    def toObservable(implicit ec: ExecutionContext): Observable[T] = {
      val subject = Subject[T]()

      val future = publisher foreach { value =>
        subject.onNext(value)
        if (!subject.hasObservers) {
          throw new RuntimeException("No-one is listening. This is very important exception. Otherwise the subscription will never stop.")
        }
      }
      future onComplete {
        case Success(_) =>
          subject.onCompleted()
        case Failure(error) =>
          subject.onError(error)
      }
      subject
    }
  }
}