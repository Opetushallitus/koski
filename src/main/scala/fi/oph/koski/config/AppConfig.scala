package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.common.log.Logging
import software.amazon.awssdk.services.appconfig.AppConfigClient
import software.amazon.awssdk.services.appconfig.model._

case class EcsMetadataResponse(DockerId: String)

object AppConfig extends Logging {
  lazy private val envName = sys.env.getOrElse("ENV_NAME", "local")

  lazy private val appConfigClient: AppConfigClient = AppConfigClient.builder
    .build

  lazy private val clientId: String = {
    sys.env.get("ECS_CONTAINER_METADATA_URI_V4") match {
      case Some(metadataEndpoint) =>
        val http = Http(metadataEndpoint, "ecsMetadata")
        runTask(http.get(uri"")(Http.parseJson[EcsMetadataResponse])).DockerId
      case None => "local"
    }
  }

  lazy private val appConfigClientRequest: GetConfigurationRequest = GetConfigurationRequest.builder
    .application("koski")
    .environment(envName)
    .configuration(envName)
    .clientId(clientId)
    .build

  def createConfig: Config = {
    val configContent = appConfigClient.getConfiguration(appConfigClientRequest)
        .content()
        .asUtf8String()
    ConfigFactory.parseString(configContent)
    }
}
