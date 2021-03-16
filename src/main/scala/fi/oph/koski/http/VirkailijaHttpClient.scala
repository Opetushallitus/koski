package fi.oph.koski.http

import com.typesafe.config.Config
import fi.oph.koski.log.NotLoggable
import org.http4s.client.Client
import fi.vm.sade.utils.cas.{CasClient, CasParams, CasAuthenticatingClient}
import fi.oph.koski.config.{Environment, SecretsManager}

case class VirkailijaCredentials(username: String, password: String) extends NotLoggable

object VirkailijaCredentials {
  def apply(serviceConfig: ServiceConfig): VirkailijaCredentials = {
    if (Environment.usesAwsSecretsManager) {
      VirkailijaCredentials.fromSecretsManager
    }
    else {
      VirkailijaCredentials(serviceConfig.username, serviceConfig.password)
    }
  }
  def fromSecretsManager: VirkailijaCredentials = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Opintopolku virkailija credentials", "OPINTOPOLKU_VIRKAILIJA_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[VirkailijaCredentials](secretId)
  }
  def fromConfig(config: Config): VirkailijaCredentials = {
    VirkailijaCredentials(
      config.getString("opintopolku.virkailija.username"),
      config.getString("opintopolku.virkailija.password")
    )
  }
}

object VirkailijaHttpClient {
  def apply(serviceConfig: ServiceConfig, serviceUrl: String, sessionCookieName: String = "JSESSIONID"): Http = {
    val VirkailijaCredentials(username, password) = VirkailijaCredentials(serviceConfig)
    val blazeHttpClient = Http.newClient(serviceUrl)
    val casAuthenticatingClient: Client = if (serviceConfig.useCas) {
      val casClient = new CasClient(serviceConfig.virkailijaUrl + "/cas", blazeHttpClient, OpintopolkuCallerId.koski)
      CasAuthenticatingClient(casClient, CasParams(serviceUrl, username, password), blazeHttpClient, OpintopolkuCallerId.koski, sessionCookieName)
    } else {
      ClientWithBasicAuthentication(blazeHttpClient, username, password)
    }
    Http(serviceConfig.virkailijaUrl, casAuthenticatingClient)
  }
}

case class ServiceConfig(virkailijaUrl: String, username: String, password: String, useCas: Boolean)

object ServiceConfig {
  def apply(config: Config, prefixes: String*): ServiceConfig = {
    ServiceConfig(
      virkailijaUrl = getString(config, prefixes, "url"),
      username = getString(config, prefixes, "username"),
      password = getString(config, prefixes, "password"),
      useCas = config.getBoolean("authentication-service.useCas")
    )
  }

  private def getString(config: Config, prefixes: Seq[String], suffix: String) = {
    val paths = prefixes.map(p => p + "." + suffix)
    paths.find(config.hasPath(_)).map(config.getString(_)).getOrElse(s"None of the following configuration options found: ${paths.mkString(",")}")
  }
}
