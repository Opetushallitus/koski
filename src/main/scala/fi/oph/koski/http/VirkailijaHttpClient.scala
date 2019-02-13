package fi.oph.koski.http

import com.typesafe.config.Config
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.client.Client

object VirkailijaHttpClient {
  def apply(serviceConfig: ServiceConfig, serviceUrl: String, useCas: Boolean = true, sessionCookieName: String = "JSESSIONID") = {
    val blazeHttpClient = Http.newClient(serviceUrl)
    val casAuthenticatingClient: Client = if (useCas) {
      val casClient = new CasClient(serviceConfig.virkailijaUrl, blazeHttpClient)
      CasAuthenticatingClient(casClient, CasParams(serviceUrl, serviceConfig.username, serviceConfig.password), blazeHttpClient, Some(OpintopolkuCallerId.koski), sessionCookieName)
    } else {
      ClientWithBasicAuthentication(blazeHttpClient, serviceConfig.username, serviceConfig.password)
    }
    Http(serviceConfig.virkailijaUrl, casAuthenticatingClient)
  }
}

case class ServiceConfig(virkailijaUrl: String, username: String, password: String)

object ServiceConfig {
  def apply(config: Config, prefixes: String*): ServiceConfig = {
    val virkailijaUrl = getString(config, prefixes, "url")
    val username = getString(config, prefixes, "username")
    val password = getString(config, prefixes, "password")

    ServiceConfig(virkailijaUrl, username, password)
  }

  private def getString(config: Config, prefixes: Seq[String], suffix: String) = {
    val paths = prefixes.map(p => p + "." + suffix)
    paths.find(config.hasPath(_)).map(config.getString(_)).getOrElse(s"None of the following configuration options found: ${paths.mkString(",")}")
  }
}
