package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus}

case class InvalidRequestException(status: HttpStatus) extends Exception(status.toString) {
  def this(category: ErrorCategory, message: String) = this(category.apply(message))
  def this(category: ErrorCategory) = this(category.apply())
}
