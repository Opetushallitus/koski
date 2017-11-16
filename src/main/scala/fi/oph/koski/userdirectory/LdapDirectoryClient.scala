package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.vm.sade.security.ldap.{LdapClient, LdapConfig, LdapUser}

case class LdapDirectoryClient(config: Config) extends DirectoryClient {
  private val langPattern = "LANG_(.*)".r
  private val ldapClient = new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password"), config.getString("ldap.port").toInt))

  override def findUser(username: String): Option[DirectoryUser] = ldapClient.findUser(username).map(convertUser)

  override def authenticate(userid: String, password: String): Boolean = ldapClient.authenticate(userid, password)

  private def convertUser(ldapUser: LdapUser) = {
    val lang = ldapUser.roles.collect {
      case langPattern(s) => s
    }.headOption

    DirectoryUser(ldapUser.oid, LdapKayttooikeudet.käyttöoikeudet(ldapUser), ldapUser.givenNames, ldapUser.lastName, lang)
  }
}