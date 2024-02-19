package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.log.Logging
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import software.amazon.awssdk.services.appconfigdata.model.{GetLatestConfigurationRequest, StartConfigurationSessionRequest}

object AppConfig extends Logging {
  lazy private val envName = sys.env.getOrElse("ENV_NAME", "local")

  lazy private val appConfigClient: AppConfigDataClient = AppConfigDataClient.builder
    .build

  lazy private val configurationSessionRequest =
    StartConfigurationSessionRequest.builder
      .applicationIdentifier("koski")
      .environmentIdentifier(envName)
      .configurationProfileIdentifier(envName)
      .build

  lazy private val configurationSession =
    appConfigClient.startConfigurationSession(configurationSessionRequest)

  private def getLatestConfigurationRequest = {
    val token = configurationToken.getOrElse(configurationSession.initialConfigurationToken())
    GetLatestConfigurationRequest.builder
      .configurationToken(token)
      .build
  }

  private var configurationToken: Option[String] = None
  private var configuration: Option[Config] = None

  def loadConfig: Option[Config] = {
    val config = appConfigClient.getLatestConfiguration(getLatestConfigurationRequest)
    configurationToken = Some(config.nextPollConfigurationToken)

    val configContent = config.configuration().asUtf8String()

    if (configContent.nonEmpty) {
      configuration = Some(ConfigFactory.parseString(configContent))
    }

    configuration
  }
}
