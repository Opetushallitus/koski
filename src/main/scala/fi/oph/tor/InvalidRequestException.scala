package fi.oph.tor

case class InvalidRequestException(msg: String) extends Exception(msg)
