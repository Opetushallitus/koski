package fi.oph.koski.config

import com.typesafe.config.Config

object ShibbolethSecret {
  def fromConfig(config: Config) = config.getString("login.security")
  def fromSecretsManager = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Shibboleth security", "SHIBBOLETH_SECRET_ID")
    cachedSecretsClient.getSecretString(secretId)
  }
}
