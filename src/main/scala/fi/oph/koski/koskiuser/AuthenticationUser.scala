package fi.oph.koski.koskiuser

import fi.vm.sade.security.ldap.LdapUser

case class AuthenticationUser(oid: String, username: String, name: String, serviceTicket: Option[String]) extends UserWithUsername

object AuthenticationUser {
  def fromLdapUser(username: String, ldapUser: LdapUser) = AuthenticationUser(ldapUser.oid, username, ldapUser.givenNames + " " + ldapUser.lastName, None)
}