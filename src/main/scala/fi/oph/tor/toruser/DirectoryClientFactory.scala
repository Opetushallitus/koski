package fi.oph.tor.toruser

import com.typesafe.config.Config
import fi.oph.tor.cache.CachingStrategy.cacheAll
import fi.oph.tor.cache.{CachingProxy, CachingStrategy}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig}

object DirectoryClientFactory {
  def directoryClient(config: Config): DirectoryClient = {
    val cacheStrategy = cacheAll(durationSeconds = 60, maxSize = 100)

    CachingProxy(cacheStrategy, if (config.hasPath("ldap")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password")))
    } else {
      MockUsers
    })
  }
}
