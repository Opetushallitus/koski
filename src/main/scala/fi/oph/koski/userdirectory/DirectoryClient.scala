package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.common.log.NotLoggable
import fi.oph.koski.cache.{CacheManager, Cached, CachingProxy, ExpiringCache}
import fi.oph.koski.koskiuser.Käyttöoikeus

import scala.concurrent.duration._

case class Password(password: String) extends NotLoggable

trait DirectoryClient {
  def findUser(username: String): Option[DirectoryUser]
  def authenticate(userid: String, wrappedPassword: Password): Boolean
}

object DirectoryClient {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): DirectoryClient with Cached = {
    val cacheStrategy = ExpiringCache("DirectoryClient", 60.seconds, maxSize = 100)
    CachingProxy[DirectoryClient](cacheStrategy, config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        MockDirectoryClient
      case url =>
        new OpintopolkuDirectoryClient(url, config)
    })
  }
}

case class DirectoryUser(oid: String, käyttöoikeudet: List[Käyttöoikeus], etunimet: String, sukunimi: String, asiointikieli: Option[String])








