package fi.oph.koski.config

import com.typesafe.config.Config
import fi.oph.koski.util.Files

object Environment {
  def isLocalDevelopmentEnvironment: Boolean = Files.exists("Makefile")

  def usesAwsAppConfig: Boolean = {
    sys.env.getOrElse("CONFIG_SOURCE", "") == "appconfig"
  }

  def currentEnvironment(config: Config): String = config.getString("env")
}
