package fi.oph.tor.http

case class HttpStatus(statusCode: Int, errors: List[String]) {
  def isOk = statusCode < 300
  def isError = !isOk
  def ++(status: HttpStatus) = HttpStatus.append(this, status)
}

object HttpStatus {
  val ok = HttpStatus(200, Nil)
  def internalError(text: String) = HttpStatus(500, List(text))
  def conflict(text: String) = HttpStatus(409, List(text))
  def badRequest(text: String) = HttpStatus(400, List(text))
  def forbidden(text: String) = HttpStatus(403, List(text))
  def notFound(text: String) = HttpStatus(404, List(text))

  def append(a: HttpStatus, b: HttpStatus) = {
    HttpStatus(Math.max(a.statusCode, b.statusCode), a.errors ++ b.errors)
  }
  def fold(statii: Iterable[HttpStatus]) = statii.fold(ok)(append)
}