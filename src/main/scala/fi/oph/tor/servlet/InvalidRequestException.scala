package fi.oph.tor.servlet

import fi.oph.tor.http.{ErrorCategory, HttpStatus}

case class InvalidRequestException(status: HttpStatus) extends Exception(status.toString) {
  def this(category: ErrorCategory, message: String) = this(category.apply(message))
}
