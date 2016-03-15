package fi.oph.tor.toruser

import com.typesafe.config.Config
import fi.oph.tor.cache.{Cached, CachingProxy}
import fi.oph.tor.cache.CachingStrategy.cacheAllNoRefresh
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig}

object DirectoryClientFactory {
  def directoryClient(config: Config): DirectoryClient with Cached = {
    val cacheStrategy = cacheAllNoRefresh(durationSeconds = 60, maxSize = 100)

    CachingProxy[DirectoryClient](cacheStrategy, if (config.hasPath("ldap")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password")))
    } else {
      MockUsers
    })
  }
}
