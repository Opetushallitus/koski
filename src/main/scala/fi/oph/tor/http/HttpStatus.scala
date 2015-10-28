package fi.oph.tor.http

case class HttpStatus(statusCode: Int, errors: List[String]) {
  def isOk = statusCode < 300
  def isError = !isOk
  def ++(status: HttpStatus) = HttpStatus.append(this, status)
  def andThen(status: => HttpStatus) = this ++ status
  def andThenIf(predicate: => Boolean)(status: => HttpStatus) = if (predicate) { this ++ status} else { this}
  def andThenIf[T](optional: => Option[T])(block: T => HttpStatus) = optional match {
    case Some(x) => this ++ block(x)
    case None => this
  }
  def ifOkThen(status: => HttpStatus) = if (isOk) { status } else { this }
}

object HttpStatus extends HttpStatus(200, Nil) {
  val ok = this
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