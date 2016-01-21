package fi.oph.tor.servlet

case class InvalidRequestException(msg: String) extends Exception(msg)
