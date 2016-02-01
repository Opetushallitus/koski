package fi.oph.tor.http

import fi.oph.tor.http.TorErrorCode.TorErrorCode
import org.json4s.JValue

case class HttpStatus(statusCode: Int, errors: List[ErrorDetail]) {
  def isOk = statusCode < 300
  def isError = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def then(status: => HttpStatus) = if (isOk) { status } else { this }
}

object HttpStatus {
  // Known HTTP statii

  val ok = HttpStatus(200, Nil)
  def internalError(text: String = "Internal server error") = HttpStatus(500, List(ErrorDetail(TorErrorCode.internalError, text)))
  def conflict(errorCode: TorErrorCode, text: String) = HttpStatus(409, List(ErrorDetail(errorCode, text)))
  def badRequest(errorCode: TorErrorCode, text: AnyRef): HttpStatus = badRequest(ErrorDetail(errorCode, text))
  def badRequest(error: ErrorDetail): HttpStatus = badRequest(List(error))
  def badRequest(errors: List[ErrorDetail]): HttpStatus = HttpStatus(400, errors)
  def forbidden(errorCode: TorErrorCode, text: String) = HttpStatus(403, List(ErrorDetail(errorCode, text)))
  def notFound(text: String) = HttpStatus(404, List(ErrorDetail(TorErrorCode.notFound, text)))

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

}