package fi.oph.koski.util

import rx.RxReactiveStreams
import rx.lang.scala.JavaConversions.toScalaObservable
import slick.backend.DatabasePublisher

object ReactiveStreamsToRx {
  implicit def publisherToObservable[T](publisher: DatabasePublisher[T]): rx.lang.scala.Observable[T] = RxReactiveStreams.toObservable(publisher)
}