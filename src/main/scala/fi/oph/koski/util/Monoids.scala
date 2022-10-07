package fi.oph.koski.util

import cats.Monoid

object Monoids {
  def seqMonoid[T]: Monoid[Seq[T]] = new Monoid[Seq[T]] {
    def empty: Seq[T] = Seq.empty
    def combine(a: Seq[T], b: Seq[T]): Seq[T] = a ++ b
  }

  def rightSeqMonoid[L, R]: Monoid[Either[L, Seq[R]]] = new Monoid[Either[L, Seq[R]]] {
    def empty: Either[L, Seq[R]] = Right(Seq.empty)
    def combine(a: Either[L, Seq[R]], b: Either[L, Seq[R]]): Either[L, Seq[R]] = (a, b) match {
      case (Right(x), Right(y)) => Right(x ++ y)
      case (Left(x), _) => Left(x)
      case (_, Left(x)) => Left(x)
    }
  }
}
