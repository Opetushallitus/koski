package fi.oph.koski.util

import fi.oph.koski.http.HttpStatus

case class WithWarnings[A](_value: A, warnings: Seq[HttpStatus]) {

  def get: A = {
    if (warnings.nonEmpty) {
      throw new IllegalStateException("WithWarnings.get when warnings is not empty")
    } else {
      _value
    }
  }

  def getIgnoringWarnings: A = _value

  def map[B](f: A => B): WithWarnings[B] =
    WithWarnings(f(_value), warnings)

  def warningsToLeft: Either[HttpStatus, A] =
    if (warnings.nonEmpty) Left(warnings.head) else Right(_value)
}
