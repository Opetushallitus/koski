package fi.oph.koski.koskiuser

case class AuthenticationUser(oid: String, name: String, serviceTicket: Option[String])