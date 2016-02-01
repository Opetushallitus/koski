package fi.oph.tor.http

case class HttpStatus(statusCode: Int, errors: List[ErrorDetail]) {
  def isOk = statusCode < 300
  def isError = !isOk

  /** Pick given status if this one is ok. Otherwise stick with this one */
  def then(status: => HttpStatus) = if (isOk) { status } else { this }
}

object HttpStatus {
  // Known HTTP statii

  val ok = HttpStatus(200, Nil)

  def apply(category: ErrorCategory, message: String): HttpStatus = HttpStatus(category.statusCode, List(ErrorDetail(category, message)))
  def apply(category: CategoryWithDefaultText): HttpStatus = apply(category, category.message)

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

case class ErrorCategory(val key: String, val statusCode: Int) {
  def this(parent: ErrorCategory, key: String) = this(parent.key + "." + key, parent.statusCode)
  def subcategory(subkey: String) = new ErrorCategory(this, subkey)
  def subcategory(subkey: String, message: String) = new CategoryWithDefaultText(this, subkey, message)
  def apply(message: AnyRef): HttpStatus = HttpStatus(statusCode, List(ErrorDetail(this, message)))
}

class CategoryWithDefaultText(key: String, status: Int, val message: String) extends ErrorCategory(key, status) {
  def this(parent: ErrorCategory, key: String, message: String) = this(parent.key + "." + key, parent.statusCode, message)
  def apply(): HttpStatus = apply(message)
}

case class ErrorDetail(category: ErrorCategory, message: AnyRef) {
  override def toString = category.key + " (" + message + ")"
}