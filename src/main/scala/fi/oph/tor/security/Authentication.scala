package fi.oph.tor.security

import com.typesafe.config.Config
import fi.oph.tor.cache.{CacheAll, CachingProxy}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig, LdapUser}
import fi.vm.sade.security.mock.MockDirectoryClient

object Authentication {
  def directoryClient(config: Config): DirectoryClient = {
    val cacheStrategy = CacheAll(durationSeconds = 60, maxSize = 100)

    CachingProxy(cacheStrategy, if (config.hasPath("ldap")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password")))
    } else {
      new MockDirectoryClient(Map("kalle" -> LdapUser(List(), "käyttäjä", "kalle", "12345"), "hiiri" -> LdapUser(List(), "käyttäjä", "hiiri", "11111")))
    })
  }
}