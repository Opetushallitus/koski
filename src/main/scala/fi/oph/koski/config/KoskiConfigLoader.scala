package fi.oph.koski.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.log.Logging

object KoskiConfigLoader extends Logging {
  // Ladataan kerran, jotta kaikki käyttävät samaa konfiguraatiota ja pilvessä AppConfig haetaan vain yhdellä AWS-kutsulla.
  lazy val config: Config =
    if (Environment.usesAwsAppConfig) loadFromAppConfig() else ConfigFactory.load()

  private def loadFromAppConfig(): Config = {
    logger.info("Ladataan konfiguraatio...")
    try {
      AppConfig
        .loadConfig
        .map(cloudConfig(_, AppConfig.envName))
        .getOrElse {
          throw new RuntimeException("Konfiguraatiota ei saatu ladattua")
        }
    } catch {
      case e: Throwable =>
        logger.error(s"Konfiguraation lataus epäonnistui: ${e.getMessage}")
        throw e
    }
  }

  // Ympäristön nimi tulee infrasta, jotta AppConfigin puuttuva tai väärä env ei muuta ympäristön tunnistusta.
  private[koski] def cloudConfig(appConfig: Config, envName: String): Config =
    ConfigFactory.load(appConfig).withValue("env", fromAnyRef(envName))
}
