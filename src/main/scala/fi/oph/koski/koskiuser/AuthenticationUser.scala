package fi.oph.koski.koskiuser

import fi.oph.koski.huoltaja.HuollettavatSearchResult
import fi.oph.koski.userdirectory.DirectoryUser

case class AuthenticationUser(
  oid: String,
  username: String,
  name: String,
  serviceTicket: Option[String],
  kansalainen: Boolean = false,
  huollettava: Boolean = false,
  huollettavat: Option[HuollettavatSearchResult] = None,
) extends UserWithUsername with UserWithOid {
  def isSuoritusjakoKatsominen: Boolean = oid == KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
  def isYtrDownloadUser: Boolean = oid == KoskiSpecificSession.KOSKI_SYSTEM_USER_TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET
}

object AuthenticationUser {
  def fromDirectoryUser(username: String, user: DirectoryUser) = AuthenticationUser(user.oid, username, user.etunimet + " " + user.sukunimi, None)
}
