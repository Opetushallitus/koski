package fi.oph.tor.security

import com.typesafe.config.Config
import fi.vm.sade.security.ldap.{LdapConfig, LdapClient, LdapUser, DirectoryClient}
import fi.vm.sade.security.mock.MockDirectoryClient

object Authentication {
  def directoryClient(config: Config): DirectoryClient = {
    if (config.hasPath("ldap")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password")))
    } else {
      new MockDirectoryClient(Map(
        "kalle" -> LdapUser(List(), "käyttäjä", "kalle", "12345"),
        "hiiri" -> LdapUser(List(), "käyttäjä", "hiiri", "11111")
      ))
    }
  }
}