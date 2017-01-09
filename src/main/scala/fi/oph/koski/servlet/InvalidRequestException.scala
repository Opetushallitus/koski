package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus}

case class InvalidRequestException(status: HttpStatus) extends Exception(status.toString)

object InvalidRequestException {
  def apply(category: ErrorCategory, message: String): InvalidRequestException = new InvalidRequestException(category.apply(message))
  def apply(category: ErrorCategory): InvalidRequestException = new InvalidRequestException(category.apply())
}