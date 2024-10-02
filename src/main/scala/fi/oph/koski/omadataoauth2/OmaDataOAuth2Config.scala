package fi.oph.koski.omadataoauth2

import com.typesafe.config.{Config => TypeSafeConfig}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import scala.collection.JavaConverters._


trait OmaDataOAuth2Config extends Logging  {
  def application: KoskiApplication
  protected def conf: TypeSafeConfig = application.config.getConfig("omadataoauth2")

  def hasConfigForClient(client_id: String): Boolean = getConfigOption(client_id).isDefined

  def hasRedirectUri(client_id: String, redirect_uri: String): Boolean = {
    getConfigOption(client_id) match {
      case Some(clientConfig) =>
        clientConfig.getStringList("redirect_uris").asScala.contains(redirect_uri)
      case _ => false
    }
  }

  private def getConfigOption(client_id: String): Option[TypeSafeConfig] = {
    conf.getConfigList("clients").asScala.find(member => member.getString("client_id") == client_id)
  }

}
