package fi.oph.koski.config

import com.typesafe.config.Config
import fi.oph.koski.util.Files

object Environment {
  val Local = "local"
  val UnitTest = "unittest"

  def isLocalDevelopmentEnvironment: Boolean = Files.exists("Makefile")

  def isUsingLocalDevelopmentServices(app: KoskiApplication): Boolean =
    app.masterDatabase.isLocal && app.config.getString("opintopolku.virkailija.url") == "mock"

  def usesAwsAppConfig: Boolean = {
    sys.env.getOrElse("CONFIG_SOURCE", "") == "appconfig"
  }

  def usesAwsSecretsManager: Boolean = {
    sys.env.getOrElse("USE_SECRETS_MANAGER", "") == "true"
  }

  def currentEnvironment(config: Config): String = config.getString("env")
}
