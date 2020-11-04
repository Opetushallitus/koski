package fi.oph.koski.config

import fi.oph.koski.json.JsonSerializer
import org.json4s.jackson.JsonMethods.parse
import scala.reflect.runtime.universe._
import com.amazonaws.secretsmanager.caching.SecretCache


object SecretsManager extends SecretCache {
  def getStructuredSecret[T: TypeTag](secretId: String): T = {
    JsonSerializer.extract[T](parse(getSecretString(secretId)), ignoreExtras = true)
  }
}
