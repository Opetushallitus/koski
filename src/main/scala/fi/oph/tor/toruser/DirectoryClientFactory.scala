package fi.oph.tor.toruser

import com.typesafe.config.Config
import fi.oph.tor.cache.{CacheAll, CachingProxy}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig}
import fi.vm.sade.security.mock.MockDirectoryClient

object DirectoryClientFactory {
  def directoryClient(config: Config): DirectoryClient = {
    val cacheStrategy = CacheAll(durationSeconds = 60, maxSize = 100)

    CachingProxy(cacheStrategy, if (config.hasPath("ldap")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password")))
    } else {
      MockUsers
    })
  }
}
