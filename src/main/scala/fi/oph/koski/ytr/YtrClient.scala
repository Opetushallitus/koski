package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{ClientWithBasicAuthentication, Http}
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.log.{Logging, TimedProxy}
import org.http4s.client.blaze.BlazeClientConfig
import org.json4s.JValue

trait YtrClient {
  def oppijaByHetu(hetu: String): Option[YtrOppija] = oppijaJsonByHetu(hetu).map(JsonSerializer.extract[YtrOppija](_, ignoreExtras = true))
  def oppijaJsonByHetu(hetu: String): Option[JValue]
}

object YtrClient extends Logging {
    def apply(config: Config): YtrClient = {
      val url = {
        if (Environment.usesAwsSecretsManager) {
          YtrConfig.fromSecretsManager.url
        } else {
          config.getString("ytr.url")
        }
      }
      url match {
        case "mock" =>
          logger.info("Using mock YTR integration")
          MockYrtClient
        case "" =>
          logger.info("YTR integration disabled")
          EmptyYtrClient
        case _ =>
          val ytrConfig = if (Environment.usesAwsSecretsManager) YtrConfig.fromSecretsManager else YtrConfig.fromConfig(config)
          logger.info(s"Using YTR integration endpoint $url ${if (ytrConfig.insecure) "INSECURE connection!"}")
          TimedProxy[YtrClient](RemoteYtrClient(ytrConfig.url, ytrConfig.username, ytrConfig.password, ytrConfig.insecure))
      }
    }
}

object EmptyYtrClient extends YtrClient {
  override def oppijaJsonByHetu(hetu: String): None.type = None
}

object MockYrtClient extends YtrClient {
  def oppijaJsonByHetu(hetu: String): Option[JValue] = JsonResources.readResourceIfExists(resourcename(hetu))
  def filename(hetu: String): String = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/" + hetu + ".json"
}

case class RemoteYtrClient(rootUrl: String, user: String, password: String, insecure: Boolean = false) extends YtrClient {
  private val clientConfig: BlazeClientConfig = if (insecure) BlazeClientConfig.insecure else BlazeClientConfig.defaultConfig
  private val http = Http(rootUrl, ClientWithBasicAuthentication(Http.newClient("ytr", clientConfig), user, password))
  def oppijaJsonByHetu(hetu: String): Option[JValue] = {
    http.get(uri"/api/oph-koski/student/$hetu")(Http.parseJsonOptional[JValue]).unsafePerformSync
  }
}

case class YtrConfig(insecure: Boolean, username: String, password: String, url: String)

object YtrConfig {
  def fromConfig(config: Config) = YtrConfig(
    config.hasPath("ytr.insecure") && config.getBoolean("ytr.insecure"),
    config.getString("ytr.username"),
    config.getString("ytr.password"),
    config.getString("ytr.url")
  )
  def fromSecretsManager: YtrConfig = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("YTR secrets", "YTR_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[YtrConfig](secretId)
  }
}
