package fi.oph.koski.http

import com.typesafe.config.Config
import fi.oph.koski.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.log.NotLoggable

case class VirkailijaCredentials(username: String, password: String) extends NotLoggable

object VirkailijaCredentials {
  def apply(serviceConfig: ServiceConfig): VirkailijaCredentials = {
    if (Environment.usesAwsSecretsManager) {
      VirkailijaCredentials.fromSecretsManager
    } else {
      VirkailijaCredentials(serviceConfig.username, serviceConfig.password)
    }
  }

  def apply(config: Config): VirkailijaCredentials = {
    if (Environment.usesAwsSecretsManager) {
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
  def apply(serviceConfig: ServiceConfig, serviceUrl: String, sessionCookieName: String = "JSESSIONID"): Http = {
    val VirkailijaCredentials(username, password) = VirkailijaCredentials(serviceConfig)
    val blazeClient = Http.newClient(serviceUrl)
    val casAuthenticatingClient = if (serviceConfig.useCas) {
      val casClient = new CasClient(serviceConfig.virkailijaUrl + "/cas", blazeClient, OpintopolkuCallerId.koski)
      CasAuthenticatingClient(
        casClient,
        CasParams(serviceUrl, username, password),
        blazeClient,
        OpintopolkuCallerId.koski,
        sessionCookieName
      )
    } else {
      ClientWithBasicAuthentication(
        blazeClient,
        username,
        password
      )
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
    paths.find(config.hasPath).map(config.getString).getOrElse(s"None of the following configuration options found: ${paths.mkString(",")}")
  }
}
