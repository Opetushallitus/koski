package fi.oph.koski.config

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import fi.oph.koski.log.Logging

import java.io.File

object KoskiConfigLoader extends Logging {
  // Luetaan polusta eikä luokkapolulta, jotta tiedosto ei päädy pakettiin eikä konttiin.
  private val LocalConfigFile = new File("src/main/conf/local.conf")

  // Ladataan kerran, jotta kaikki käyttävät samaa konfiguraatiota ja pilvessä AppConfig haetaan vain yhdellä AWS-kutsulla.
  lazy val config: Config =
    if (Environment.usesAwsAppConfig) loadFromAppConfig() else loadLocal()

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

  private[koski] def loadLocal(file: File = LocalConfigFile): Config =
    localConfig(
      application = ConfigFactory.defaultApplication(),
      local = ConfigFactory.parseFile(file, ConfigParseOptions.defaults().setAllowMissing(false))
    )

  // Kerrokset kuten ConfigFactory.loadissa, mutta local.conf on application.confin (tai -Dconfig.resourcen) ja
  // reference.confin välissä.
  private[koski] def localConfig(application: Config, local: Config): Config =
    ConfigFactory.defaultOverrides()
      .withFallback(application)
      .withFallback(local)
      .withFallback(ConfigFactory.defaultReferenceUnresolved())
      .resolve()
}
