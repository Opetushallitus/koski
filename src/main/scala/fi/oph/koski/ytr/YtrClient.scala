package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{ClientWithBasicAuthentication, Http}
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging
import org.json4s.JValue

trait YtrClient {
  implicit val formats = Json.jsonFormats
  def oppijaByHetu(hetu: String): Option[YtrOppija] = oppijaJsonByHetu(hetu).map(_.extract[YtrOppija])
  def oppijaJsonByHetu(hetu: String): Option[JValue]
}

object YtrClient extends Logging {
  def apply(config: Config) = config.getString("ytr.url") match {
    case "mock" =>
      logger.info("Using mock YTR integration")
      YtrMock
    case "" =>
      logger.info("YTR integration disabled")
      YtrEmpty
    case _ =>
      logger.info("Using YTR integration endpoint " + config.getString("ytr.url"))
      YtrRemote(config.getString("ytr.url"), config.getString("ytr.username"), config.getString("ytr.password"))
  }
}

object YtrEmpty extends YtrClient {
  override def oppijaJsonByHetu(hetu: String) = None
}

object YtrMock extends YtrClient {
  def oppijaJsonByHetu(hetu: String): Option[JValue] = Json.readResourceIfExists(resourcename(hetu))
  def filename(hetu: String) = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/" + hetu + ".json"
}

case class YtrRemote(rootUrl: String, user: String, password: String) extends YtrClient {
  private val http = Http(rootUrl, ClientWithBasicAuthentication(Http.newClient, user, password))
  def oppijaJsonByHetu(hetu: String): Option[JValue] = http.get(uri"/api/oph-transfer/student/${hetu}")(Http.parseJsonOptional[JValue]).run
}