package fi.oph.tor.db

import rx.RxReactiveStreams
import rx.lang.scala.JavaConversions.toScalaObservable
import slick.backend.DatabasePublisher

object StreamExtensions {
  implicit def publisherToObservable[T](publisher: DatabasePublisher[T]): rx.lang.scala.Observable[T] = RxReactiveStreams.toObservable(publisher)
}