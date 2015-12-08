package fi.oph.tor.http

import scala.collection.GenTraversableOnce

case class HttpStatus(statusCode: Int, errors: List[String]) {
  def isOk = statusCode < 300
  def isError = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def then(status: => HttpStatus) = if (isOk) { status } else { this }
}

object HttpStatus {
  // Known HTTP statii

  val ok = HttpStatus(200, Nil)
  def internalError(text: String = "Internal server error") = HttpStatus(500, List(text))
  def conflict(text: String) = HttpStatus(409, List(text))
  def badRequest(text: String) = HttpStatus(400, List(text))
  def forbidden(text: String) = HttpStatus(403, List(text))
  def notFound(text: String) = HttpStatus(404, List(text))

  // Combinators

  /** If predicate is true, yield 200/ok, else run given block */
  def validate(predicate: => Boolean)(status: => HttpStatus) = if (predicate) { ok } else { status }
  /** Map each object to a status, fold statii */
  def each[T](xs: Iterable[T])(block: T => HttpStatus) = fold(xs.map(block))
  /** Combine two statii: concatenate errors list, pick highest status code */
  def append(a: HttpStatus, b: HttpStatus) = {
    HttpStatus(Math.max(a.statusCode, b.statusCode), a.errors ++ b.errors)
  }
  /** Append all given statii into one, concatenating error list, picking highest status code */
  def fold(statii: Iterable[HttpStatus]) = statii.fold(ok)(append)
}