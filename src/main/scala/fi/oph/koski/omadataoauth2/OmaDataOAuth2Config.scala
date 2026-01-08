package fi.oph.koski.omadataoauth2

import com.typesafe.config.{Config => TypeSafeConfig}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import scala.jdk.CollectionConverters._


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

  def getTokenDurationMinutes(client_id: String): Int = {
    getConfigOption(client_id) match {
      case Some(config) if config.hasPath("token_duration_minutes") =>
        config.getInt("token_duration_minutes")
      case _ => 10
    }
  }

  private def getConfigOption(client_id: String): Option[TypeSafeConfig] = {
    conf.getConfigList("clients").asScala.find(member => member.getString("client_id") == client_id)
  }

  def useFormActionCspHeader(client_id: String): Boolean = {
    getConfigOption(client_id) match {
      case Some(clientConfig) =>
        clientConfig.getBoolean("security.use_form_action_csp_header")
      case _ => true
    }
  }

  def useLogoutBeforeRedirect(client_id: String): Boolean = {
    getConfigOption(client_id) match {
      case Some(config) if config.hasPath("logout_before_redirect") =>
        config.getBoolean("logout_before_redirect")
      case _ => true
    }
  }
}
