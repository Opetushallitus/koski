package fi.oph.koski.config

import com.typesafe.config.Config
import fi.oph.koski.util.Files

object Environment {
  def isLocalDevelopmentEnvironment: Boolean = Files.exists("Makefile")

  def usesAwsAppConfig: Boolean = {
    sys.env.getOrElse("CONFIG_SOURCE", "") == "appconfig"
  }

  def usesAwsSecretsManager: Boolean = {
    sys.env.getOrElse("USE_SECRETS_MANAGER", "") == "true"
  }

  def currentEnvironment(config: Config): String = config.getString("env")
}
