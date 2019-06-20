package fi.oph.koski.ytr

import com.typesafe.config.Config
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
  def apply(config: Config) = config.getString("ytr.url") match {
    case "mock" =>
      logger.info("Using mock YTR integration")
      MockYrtClient
    case "" =>
      logger.info("YTR integration disabled")
      EmptyYtrClient
    case url =>
      val insecure = config.hasPath("ytr.insecure") && config.getBoolean("ytr.insecure")
      logger.info(s"Using YTR integration endpoint $url ${if (insecure) "INSECURE connection!"}")
      TimedProxy[YtrClient](RemoteYtrClient(url, config.getString("ytr.username"), config.getString("ytr.password"), insecure))
  }
}

object EmptyYtrClient extends YtrClient {
  override def oppijaJsonByHetu(hetu: String) = None
}

object MockYrtClient extends YtrClient {
  def oppijaJsonByHetu(hetu: String): Option[JValue] = JsonResources.readResourceIfExists(resourcename(hetu))
  def filename(hetu: String) = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/" + hetu + ".json"
}

case class RemoteYtrClient(rootUrl: String, user: String, password: String, insecure: Boolean = false) extends YtrClient {
  private val clientConfig: BlazeClientConfig = if (insecure) BlazeClientConfig.insecure else BlazeClientConfig.defaultConfig
  private val http = Http(rootUrl, ClientWithBasicAuthentication(Http.newClient("ytr", clientConfig), user, password))
  def oppijaJsonByHetu(hetu: String): Option[JValue] = {
    http.get(uri"/api/oph-transfer/student/${hetu}")(Http.parseJsonOptional[JValue]).run
  }
}
