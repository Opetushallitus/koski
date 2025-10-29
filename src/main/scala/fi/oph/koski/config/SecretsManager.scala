package fi.oph.koski.config

import fi.oph.koski.json.JsonSerializer
import org.json4s.jackson.JsonMethods.parse

import scala.reflect.runtime.universe._
import com.amazonaws.secretsmanager.caching.SecretCache
import fi.oph.koski.log.{Logging, NotLoggable}

case class DatabaseConnectionConfig(
  host: String,
  port: Int,
  username: String,
  password: String
) extends NotLoggable

class SecretsManager extends SecretCache with Logging {
  def getDatabaseSecret(secretId: String): DatabaseConnectionConfig =
    getStructuredSecret[DatabaseConnectionConfig](secretId)

  def getStructuredSecret[T: TypeTag](secretId: String): T = {
    logger.debug(s"Searching for secret $secretId")
    JsonSerializer.extract[T](parse(getSecretString(secretId)), ignoreExtras = true)
  }

  def getPlainSecret(secretId: String): String = {
    logger.debug(s"Fetching plain secret $secretId")
    getSecretString(secretId).stripPrefix("\"").stripSuffix("\"")
  }

  def getSecretId(secretName: String, envVar: String): String = {
    sys.env.get(envVar) match {
      case Some(envVar) => envVar
      case _ => throw new RuntimeException(
        s"Secrets manager enabled for $secretName but environment variable $envVar not set!")
    }
  }
}
