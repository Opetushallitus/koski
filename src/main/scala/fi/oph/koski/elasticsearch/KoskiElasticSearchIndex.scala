package fi.oph.koski.elasticsearch

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.{Json4sHttp4s, JsonDiff}
import fi.oph.koski.log.Logging
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotStatistics
import org.http4s.EntityEncoder
import org.json4s.jackson.JsonMethods
import org.json4s.{JValue, _}

class KoskiElasticSearchIndex(val indexName: String, val settings: JValue, val elastic: ElasticSearch) extends Logging {
  def http = elastic.http
  def refreshIndex = elastic.refreshIndex
  def reindexingNeededAtStartup = init

  lazy val init = {
    if (indexExists) {
      logger.info(s"ElasticSearch index $indexName exists")
      val serverSettings = (Http.runTask(http.get(uri"/$indexName/_settings")(Http.parseJson[JValue])) \ s"$indexName" \ "settings" \ "index")
      val mergedSettings = serverSettings.merge(settings)
      val alreadyApplied = mergedSettings == serverSettings
      if (alreadyApplied) {
        logger.info("Elasticsearch index settings are up to date")
        false
      } else {
        val diff = JsonDiff.jsonDiff(serverSettings, mergedSettings)
        logger.info(s"Updating Elasticsearch index settings (diff: ${JsonMethods.pretty(diff)})")
        Http.runTask(http.post(uri"/$indexName/_close", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        Http.runTask(http.put(uri"/$indexName/_settings", settings)(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
        Http.runTask(http.post(uri"/$indexName/_open", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        logger.info("Updated Elasticsearch index settings. Re-indexing is needed.")
        true
      }
    } else {
      logger.info(s"Creating Elasticsearch index $indexName")
      Http.runTask(http.put(uri"/$indexName", JObject("settings" -> settings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Created index.")
      true
    }
  }

  def runSearch(tyep: String, doc: JValue): Option[JValue] = try {
    Some(Http.runTask(http.post(uri"/$indexName/${tyep}/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }

  def updateBulk(jsonLines: Seq[JValue], refreshIndex: Boolean = false): (Boolean, JValue) = {
    val url = if (refreshIndex) uri"/$indexName/_bulk?refresh=wait_for" else uri"/$indexName/_bulk"
    val response: JValue = Http.runTask(http.post(url, jsonLines)(Json4sHttp4s.multiLineJson4sEncoderOf[JValue])(Http.parseJson[JValue]))
    (extract[Boolean](response \ "errors"), response)
  }

  private def indexExists = {
    Http.runTask(http.get(uri"/$indexName")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode =>
        throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  def indexIsLarge: Boolean = {
    OpiskeluoikeudenPerustiedotStatistics(this).statistics.opiskeluoikeuksienMäärä > 100
  }
}
