package fi.oph.koski.config

import fi.oph.koski.json.JsonSerializer
import org.json4s.jackson.JsonMethods.parse
import scala.reflect.runtime.universe._
import com.amazonaws.secretsmanager.caching.SecretCache
import fi.oph.koski.log.Logging



class SecretsManager extends SecretCache with Logging {
  def getStructuredSecret[T: TypeTag](secretId: String): T = {
    logger.info(s"Searching for secret $secretId")
    JsonSerializer.extract[T](parse(getSecretString(secretId)), ignoreExtras = true)
  }
}
