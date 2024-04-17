package fi.oph.koski.util

import java.time.LocalDate
import scala.language.implicitConversions


// Repurposed from the scala 2.13 backport in https://github.com/bigwheel/util-backports

object ChainingSyntax {
  implicit final def chainingOps[A](a: A): ChainingOps[A] = new ChainingOps(a)

  implicit final def eitherChainingOps[S, T](e: Either[S, T]): EitherChainingOps[S, T] = new EitherChainingOps(e)

  implicit final def stringOps(s: String): StringChainingOps = new StringChainingOps(s)

  implicit final def localDateOps(d: LocalDate): LocalDateChainingOps = new LocalDateChainingOps(d)

  implicit final def iteratorOps[T](i: Iterator[T]): IteratorChainingOps[T] = new IteratorChainingOps(i)
}

final class ChainingOps[A](private val self: A) extends AnyVal {
  def tap[U](f: A => U): A = {
    f(self)
    self
  }

  def map[U](f: A => U): U = f(self)
}

final class EitherChainingOps[S, T](private val self: Either[S, T]) extends AnyVal {
  def tap[U](f: T => U): Either[S, T] = {
    self.foreach(f)
    self
  }

  def tapLeft[U](f: S => U): Either[S, T] = {
    self.left.foreach(f)
    self
  }
}

final class StringChainingOps(private val self: String) extends AnyVal {
  def autowrap(width: Int): String =
    self
      .split("\\s+")
      .foldLeft(Seq(Seq.empty[String]))((acc, word) => {
        val init :+ last = acc
        val newLastRow = last :+ word
        if (newLastRow.mkString(" ").length > width) {
          acc :+ Seq(word)
        } else {
          init :+ newLastRow
        }
      })
      .map(_.mkString(" "))
      .mkString("\n")
}

final class LocalDateChainingOps(private val self: LocalDate) extends AnyVal {
  def isEqualOrAfter(d: LocalDate): Boolean = !self.isBefore(d)
  def isEqualOrBefore(d: LocalDate): Boolean = !self.isAfter(d)

  def atEndOfYear: LocalDate = self.withMonth(12).withDayOfMonth(31)
}

final class IteratorChainingOps[T](private val self: Iterator[T]) extends AnyVal {
  def interleave(other: Iterator[T]): Iterator[T] =
    new Iterator[T] {
      private var nextSelf: Boolean = true

      def hasNext: Boolean = self.hasNext || other.hasNext

      override def next(): T =
        if (nextSelf && self.hasNext) {
          nextSelf = false
          self.next()
        } else if (other.hasNext) {
          nextSelf = true
          other.next()
        } else {
          throw new IndexOutOfBoundsException("Both iterators are empty")
        }
    }

}
