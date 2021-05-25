package fi.oph.koski.util

import scala.language.implicitConversions


// Repurposed from the scala 2.13 backport in https://github.com/bigwheel/util-backports

object ChainingSyntax {
  implicit final def chainingOps[A](a: A): ChainingOps[A] = new ChainingOps(a)

  implicit final def eitherChainingOps[S, T](e: Either[S, T]): EitherChainingOps[S, T] = new EitherChainingOps(e)
}

final class ChainingOps[A](private val self: A) extends AnyVal {
  def tap[U](f: A => U): A = {
    f(self)
    self
  }
}

final class EitherChainingOps[S, T](private val self: Either[S, T]) extends AnyVal {
  def tap[U](f: T => U): Either[S, T] = {
    self.foreach(f)
    self
  }
}
