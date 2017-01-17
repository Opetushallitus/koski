package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{ClientWithBasicAuthentication, Http}
import fi.oph.koski.json.Json
import org.json4s.JValue

trait YlioppilasTutkintoRekisteri {
  implicit val formats = Json.jsonFormats
  def oppijaByHetu(hetu: String): Option[YtrOppija] = oppijaJsonByHetu(hetu).map(_.extract[YtrOppija])
  def oppijaJsonByHetu(hetu: String): Option[JValue]
}

object YlioppilasTutkintoRekisteri {
  def apply(config: Config) = config.getString("ytr.url") match {
    case "mock" => YtrMock
    case _ => YtrRemote(config.getString("ytr.url"), config.getString("ytr.username"), config.getString("ytr.password"))
  }
}

object YtrMock extends YlioppilasTutkintoRekisteri {
  def oppijaJsonByHetu(hetu: String): Option[JValue] = Json.readResourceIfExists(resourcename(hetu))
  def filename(hetu: String) = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/" + hetu + ".json"
}

case class YtrRemote(rootUrl: String, user: String, password: String) extends YlioppilasTutkintoRekisteri {
  private val http = Http(rootUrl, ClientWithBasicAuthentication(Http.newClient, user, password))
  def oppijaJsonByHetu(hetu: String): Option[JValue] = http.get(uri"/api/oph-transfer/student/${hetu}")(Http.parseJsonOptional[JValue]).run
}