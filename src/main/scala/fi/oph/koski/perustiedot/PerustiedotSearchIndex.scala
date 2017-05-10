package fi.oph.koski.perustiedot

import com.typesafe.config.Config
import fi.oph.koski.elasticsearch.ElasticSearchRunner
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.{GenericJsonFormats, Json4sHttp4s, LocalDateSerializer}
import fi.oph.koski.localization.LocalizedStringDeserializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoodiViiteDeserializer
import org.json4s._

object PerustiedotSearchIndex {
  implicit val formats = GenericJsonFormats.genericFormats + LocalDateSerializer + LocalizedStringDeserializer + KoodiViiteDeserializer
}
class PerustiedotSearchIndex(config: Config) extends Logging {
  import PerustiedotSearchIndex._
  private val host = config.getString("elasticsearch.host")
  private val port = config.getInt("elasticsearch.port")
  private val url = s"http://$host:$port"

  val elasticSearchHttp = Http(url)

  val init_ = OpiskeluoikeudenPerustiedotRepository.synchronized {
    if (host == "localhost") {
      new ElasticSearchRunner("./elasticsearch", port, port + 100).start
    }
    logger.info(s"Using elasticsearch at $host:$port")
  }

  def runSearch(doc: JValue): Option[JValue] = try {
    Some(Http.runTask(elasticSearchHttp.post(uri"/koski/perustiedot/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }
}
