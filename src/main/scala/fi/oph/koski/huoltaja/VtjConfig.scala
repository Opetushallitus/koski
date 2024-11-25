package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.SecretsManager
import fi.oph.koski.log.NotLoggable

object VtjConfig {
  def fromAppConfig(config: Config): VtjConfig = VtjConfig(
    serviceUrl = config.getString("vtj.serviceUrl"),
    username = config.getString("vtj.username"),
    password = config.getString("vtj.password"),
    keystorePassword = config.getString("vtj.keystorePassword"),
    keystore = config.getString("vtj.keystore"),
    truststore = config.getString("vtj.truststore"),
  )

  def fromSecretsManager: VtjConfig = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("VTJ Secrets", "VTJ_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[VtjConfig](secretId)
  }
}

case class VtjConfig(
  serviceUrl: String,
  username: String,
  password: String,
  keystore: String,
  truststore: String,
  keystorePassword: String
) extends NotLoggable
