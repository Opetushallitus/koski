package fi.oph.koski.config

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{Logging, NotLoggable}
import org.json4s.jackson.JsonMethods.parse
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import java.time.{Duration, Instant}
import java.util.concurrent.ConcurrentHashMap
import scala.reflect.runtime.universe._

case class DatabaseConnectionConfig(
  host: String,
  port: Int,
  username: String,
  password: String
) extends NotLoggable

object SecretsManager extends Logging {
  private val ttl: Duration = Duration.ofHours(1)

  private lazy val client: SecretsManagerClient =
    SecretsManagerClient.builder().region(Region.EU_WEST_1).build()

  private val cache: ConcurrentHashMap[String, CachedSecret] = new ConcurrentHashMap()

  private case class CachedSecret(value: String, fetchedAt: Instant)

  private[config] def fetch(secretId: String): String = {
    val now = Instant.now()
    val existing = cache.get(secretId)
    if (existing != null && Duration.between(existing.fetchedAt, now).compareTo(ttl) < 0) {
      existing.value
    } else {
      // computeIfAbsent gives single-flight per key; we evict expired entries first
      // so the next caller refreshes.
      if (existing != null) cache.remove(secretId, existing)
      cache.computeIfAbsent(secretId, id => CachedSecret(loadFromAws(id), Instant.now())).value
    }
  }

  private def loadFromAws(secretId: String): String = {
    logger.debug(s"Fetching secret $secretId from AWS Secrets Manager")
    val req = GetSecretValueRequest.builder().secretId(secretId).build()
    client.getSecretValue(req).secretString()
  }
}

class SecretsManager extends Logging {
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
      case Some(value) => value
      case _ => throw new RuntimeException(
        s"Secrets manager enabled for $secretName but environment variable $envVar not set!")
    }
  }

  protected def getSecretString(secretId: String): String = SecretsManager.fetch(secretId)
}
