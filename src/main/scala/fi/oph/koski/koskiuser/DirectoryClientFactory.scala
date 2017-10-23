package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.cache.{CacheManager, Cached, CachingProxy, ExpiringCache}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapClient, LdapConfig}

import scala.concurrent.duration._

object DirectoryClientFactory {
  def directoryClient(config: Config)(implicit cacheInvalidator: CacheManager): DirectoryClient with Cached = {
    val cacheStrategy = ExpiringCache("DirectoryClient", 60 seconds, maxSize = 100)
    CachingProxy[DirectoryClient](cacheStrategy, config.getString("ldap.host") match {
      case "mock" => MockDirectoryClient
      case host => new LdapClient(LdapConfig(host, config.getString("ldap.userdn"), config.getString("ldap.password"), config.getString("ldap.port").toInt))
    })
  }
}

object MockDirectoryClient extends DirectoryClient {
  def findUser(username: String) = {
    MockUsers.users.find(_.username == username).map(_.ldapUser)
  }
  def authenticate(userid: String, password: String) = findUser(userid).isDefined && userid == password
}