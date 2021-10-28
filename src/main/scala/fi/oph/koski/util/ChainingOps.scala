package fi.oph.koski.util

import scala.language.implicitConversions


// Repurposed from the scala 2.13 backport in https://github.com/bigwheel/util-backports

object ChainingSyntax {
  implicit final def chainingOps[A](a: A): ChainingOps[A] = new ChainingOps(a)

  implicit final def eitherChainingOps[S, T](e: Either[S, T]): EitherChainingOps[S, T] = new EitherChainingOps(e)

  implicit final def stringOps(s: String): StringChainingOps = new StringChainingOps(s)
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
