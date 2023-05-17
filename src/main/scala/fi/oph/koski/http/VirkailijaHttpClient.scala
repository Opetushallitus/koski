package fi.oph.koski.http

import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.log.NotLoggable
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.client.Client

case class VirkailijaCredentials(username: String, password: String) extends NotLoggable

object VirkailijaCredentials {
  def apply(serviceConfig: ServiceConfig, preferGettingCredentialsFromSecretsManager: Boolean): VirkailijaCredentials = {
    if (Environment.usesAwsSecretsManager && preferGettingCredentialsFromSecretsManager) {
      VirkailijaCredentials.fromSecretsManager
    } else {
      VirkailijaCredentials(serviceConfig.username, serviceConfig.password)
    }
  }

  def apply(config: Config, preferGettingCredentialsFromSecretsManager: Boolean): VirkailijaCredentials = {
    if (Environment.usesAwsSecretsManager && preferGettingCredentialsFromSecretsManager) {
      VirkailijaCredentials.fromSecretsManager
    } else {
      VirkailijaCredentials.fromConfig(config)
    }
  }

  private def fromSecretsManager: VirkailijaCredentials = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Opintopolku virkailija credentials", "OPINTOPOLKU_VIRKAILIJA_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[VirkailijaCredentials](secretId)
  }

  private def fromConfig(config: Config): VirkailijaCredentials = {
    VirkailijaCredentials(
      config.getString("opintopolku.virkailija.username"),
      config.getString("opintopolku.virkailija.password")
    )
  }
}

object VirkailijaHttpClient {
  private val DefaultSessionCookieName = "JSESSIONID"
  private val DefaultServiceUrlSuffix = "j_spring_cas_security_check"

  private def defaultClient(serviceUrl: String): Client[IO] = Http.retryingClient(serviceUrl)

  def apply(serviceConfig: ServiceConfig, serviceUrl: String, preferGettingCredentialsFromSecretsManager: Boolean): Http =
    apply(serviceConfig, serviceUrl, defaultClient(serviceUrl), preferGettingCredentialsFromSecretsManager)

  def apply(serviceConfig: ServiceConfig, serviceUrl: String, sessionCookieName: String, preferGettingCredentialsFromSecretsManager: Boolean): Http =
    apply(serviceConfig, serviceUrl, defaultClient(serviceUrl), preferGettingCredentialsFromSecretsManager, sessionCookieName)

  def apply(serviceConfig: ServiceConfig,
            serviceUrl: String,
            sessionCookieName: String,
            serviceUrlSuffix: String,
            preferGettingCredentialsFromSecretsManager: Boolean): Http =
    apply(serviceConfig, serviceUrl, defaultClient(serviceUrl), preferGettingCredentialsFromSecretsManager, sessionCookieName, serviceUrlSuffix)

  def apply(
    serviceConfig: ServiceConfig,
    serviceUrl: String,
    client: Client[IO],
    preferGettingCredentialsFromSecretsManager: Boolean = true,
    sessionCookieName: String = DefaultSessionCookieName,
    serviceUrlSuffix: String = DefaultServiceUrlSuffix,
  ): Http = {
    val VirkailijaCredentials(username, password) = VirkailijaCredentials(serviceConfig, preferGettingCredentialsFromSecretsManager)
    val casAuthenticatingClient = if (serviceConfig.useCas) {
      val casClient = new CasClient(serviceConfig.virkailijaUrl + "/cas", client, OpintopolkuCallerId.koski)
      CasAuthenticatingClient(
        casClient,
        CasParams(serviceUrl, serviceUrlSuffix, username, password),
        client,
        OpintopolkuCallerId.koski,
        sessionCookieName
      )
    } else {
      ClientWithBasicAuthentication(
        client,
        username,
        password
      )
    }
    Http(serviceConfig.virkailijaUrl, casAuthenticatingClient)
  }
}

case class ServiceConfig(virkailijaUrl: String, username: String, password: String, useCas: Boolean) {
  def withUrl(url: Option[String]): ServiceConfig = this.copy(virkailijaUrl = url.getOrElse(virkailijaUrl))
}

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
    paths.find(config.hasPath).map(config.getString).getOrElse(s"None of the following configuration options found: ${paths.mkString(",")}")
  }
}
