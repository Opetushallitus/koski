package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.cache.Cache.cacheAllNoRefresh
import fi.oph.koski.cache.{Cached, CachingProxy}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig}

object DirectoryClientFactory {
  def directoryClient(config: Config): DirectoryClient with Cached = {
    val cacheStrategy = cacheAllNoRefresh("DirectoryClient", durationSeconds = 60, maxSize = 100)

    CachingProxy[DirectoryClient](cacheStrategy, if (config.hasPath("ldap.host")) {
      new LdapClient(LdapConfig(config.getString("ldap.host"), config.getString("ldap.userdn"), config.getString("ldap.password"), config.getString("ldap.port").toInt))
    } else {
      MockDirectoryClient
    })
  }
}

object MockDirectoryClient extends DirectoryClient {
  def findUser(userid: String) = MockUsers.users.find(_.username == userid).map(_.ldapUser)
  def authenticate(userid: String, password: String) = findUser(userid).isDefined && userid == password
}