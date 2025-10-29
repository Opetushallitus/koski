package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.SecretsManager
import fi.oph.koski.log.NotLoggable

// ---------------------------
// 1. Config (structured JSON)
// ---------------------------
object VtjConfig {
  def fromAppConfig(config: Config): VtjConfig =
    VtjConfig(
      serviceUrl = config.getString("vtj.serviceUrl"),
      username = config.getString("vtj.username"),
      password = config.getString("vtj.password"),
      keystorePassword = config.getString("vtj.keystorePassword"),
      truststorePassword = config.getString("vtj.truststorePassword")
    )

  def fromSecretsManager(secretId: String): VtjConfig = {
    val sm = new SecretsManager
    sm.getStructuredSecret[VtjConfig](secretId)
  }
}

case class VtjConfig(
  serviceUrl: String,
  username: String,
  password: String,
  keystorePassword: String,
  truststorePassword: String
) extends NotLoggable

// ---------------------------
// 2. Keystore (base64 string)
// ---------------------------
object VtjKeystore {
  def fromAppConfig(config: Config): VtjKeystore =
    VtjKeystore(config.getString("vtj.keystore"))

  def fromSecretsManager(secretId: String): VtjKeystore = {
    val sm = new SecretsManager
    val b64 = sm.getPlainSecret(secretId)
    VtjKeystore(b64)
  }
}

case class VtjKeystore(keystore: String) extends NotLoggable

// ---------------------------
// 3. Truststore (base64 string)
// ---------------------------
object VtjTruststore {
  def fromAppConfig(config: Config): VtjTruststore =
    VtjTruststore(config.getString("vtj.truststore"))

  def fromSecretsManager(secretId: String): VtjTruststore = {
    val sm = new SecretsManager
    val b64 = sm.getPlainSecret(secretId)
    VtjTruststore(b64)
  }
}

case class VtjTruststore(truststore: String) extends NotLoggable
