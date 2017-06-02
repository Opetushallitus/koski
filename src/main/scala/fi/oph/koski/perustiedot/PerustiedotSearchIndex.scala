package fi.oph.koski.perustiedot

import fi.oph.koski.elasticsearch.ElasticSearch
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
class PerustiedotSearchIndex(elasticSearch: ElasticSearch) extends Logging {
  import PerustiedotSearchIndex._
  val elasticSearchHttp = elasticSearch.http

  def runSearch(doc: JValue): Option[JValue] = try {
    Some(Http.runTask(elasticSearchHttp.post(uri"/koski/perustiedot/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }
}