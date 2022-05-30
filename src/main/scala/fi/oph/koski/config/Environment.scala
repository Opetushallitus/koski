package fi.oph.koski.config

import com.typesafe.config.Config

object Environment {
  val Local = "local"
  val UnitTest = "unittest"

  def isUnitTestEnvironment(config: Config): Boolean = currentEnvironment(config) == UnitTest

  def isLocalDevelopmentEnvironment(config: Config): Boolean = currentEnvironment(config) == Local

  def isUsingLocalDevelopmentServices(app: KoskiApplication): Boolean =
    app.masterDatabase.isLocal && isMockEnvironment(app.config)

  def isMockEnvironment(config: Config): Boolean =
    config.getString("opintopolku.virkailija.url") == "mock"

  def isServerEnvironment(config: Config): Boolean = !Set(Local, UnitTest).contains(currentEnvironment(config))

  def usesAwsAppConfig: Boolean = {
    sys.env.getOrElse("CONFIG_SOURCE", "") == "appconfig"
  }

  def usesAwsSecretsManager: Boolean = {
    sys.env.getOrElse("USE_SECRETS_MANAGER", "") == "true"
  }

  def currentEnvironment(config: Config): String = config.getString("env")
}
