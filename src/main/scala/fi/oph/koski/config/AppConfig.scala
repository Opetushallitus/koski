package fi.oph.koski.config

import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.log.Logging
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.nio.charset.StandardCharsets

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

  def writeConfiguration: Any = {
      val configFilePath = sys.props.getOrElse("config.file", "koski.conf")

      val configContent = appConfigClient.getConfiguration(appConfigClientRequest)
        .content()
        .asUtf8String()

      Files.write(Paths.get(configFilePath), configContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE)
    }
}
