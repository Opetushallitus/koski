package fi.oph.koski.http

import fi.oph.koski.json.Json

case class HttpStatus(statusCode: Int, errors: List[ErrorDetail]) {
  if (statusCode == 200 && errors.nonEmpty) throw new RuntimeException("HttpStatus 200 with error message " + errors.mkString(","))
  def isOk = statusCode < 300
  def isError = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def then(status: => HttpStatus) = if (isOk) { status } else { this }
}

case class ErrorDetail(key: String, message: AnyRef) {
  override def toString = key + " (" + Json.write(message) + ")"
}

object HttpStatus {
  val ok = HttpStatus(200, Nil)

  // Combinators

  /** If predicate is true, yield 200/ok, else run given block */
  def validate(predicate: => Boolean)(status: => HttpStatus) = if (predicate) { ok } else { status }
  /** Combine two statii: concatenate errors list, pick highest status code */
  def append(a: HttpStatus, b: HttpStatus) = {
    HttpStatus(Math.max(a.statusCode, b.statusCode), a.errors ++ b.errors)
  }
  /** Append all given statii into one, concatenating error list, picking highest status code */
  def fold(statii: Iterable[HttpStatus]): HttpStatus = statii.fold(ok)(append)

  /** Append all given statii into one, concatenating error list, picking highest status code */
  def fold(statii: HttpStatus*): HttpStatus = fold(statii.toList)

  def foldEithers[T](xs: Iterable[Either[HttpStatus, T]]): Either[HttpStatus, List[T]] = xs.collect { case Left(e) => e} match {
    case Nil =>
      Right(xs.collect { case Right(oo) => oo }.toList)
    case errors =>
      Left(HttpStatus.fold(errors))
  }

  def justStatus[A](either: Either[HttpStatus, A]) = either match {
    case Right(_) => HttpStatus.ok
    case Left(status) => status
  }
}