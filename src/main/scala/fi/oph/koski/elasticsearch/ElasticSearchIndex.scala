package fi.oph.koski.elasticsearch

import com.typesafe.config.Config
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.{Json4sHttp4s, JsonDiff}
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import org.json4s.jackson.JsonMethods
import org.json4s.{JValue, _}

class ElasticSearchIndex(
  val elastic: ElasticSearch,
  val config: Config,
  val name: String,
  val mappingType: String,
  val settings: JValue
) extends Logging {
  def http = elastic.http

  lazy val init = {
    val indexChanged = if (indexExists) {
      migrateIndex
    } else {
      createIndex
    }

    val reindexingNeeded = indexChanged || config.getBoolean("elasticsearch.reIndexAtStartup")
    if (reindexingNeeded) {
      reindex
    }
  }

  private def indexExists = {
    Http.runTask(http.get(uri"/${name}")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode =>
        throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  private def migrateIndex: Boolean = {
    logger.info("ElasticSearch index exists")
    val serverSettings = (Http.runTask(http.get(uri"/${name}/_settings")(Http.parseJson[JValue])) \ name \ "settings" \ "index")
    val mergedSettings = serverSettings.merge(settings)
    val alreadyApplied = mergedSettings == serverSettings
    if (alreadyApplied) {
      logger.info("Elasticsearch index settings are up to date")
      false
    } else {
      val diff = JsonDiff.jsonDiff(serverSettings, mergedSettings)
      updateIndexSettings(diff)
    }
  }

  private def updateIndexSettings(newSettings: JValue) = {
    logger.info(s"Updating Elasticsearch index settings (diff: ${JsonMethods.pretty(newSettings)})")
    Http.runTask(http.post(uri"/${name}/_close", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
    Http.runTask(http.put(uri"/${name}/_settings", settings)(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
    Http.runTask(http.post(uri"/${name}/_open", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
    logger.info("Updated Elasticsearch index settings. Re-indexing is needed.")
    true
  }

  private def createIndex: Boolean = {
    logger.info("Creating Elasticsearch index")
    Http.runTask(http.put(uri"/${name}", JObject("settings" -> settings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
    true
  }

  protected def reindex: Unit = {
    throw new NotImplementedError("Reindexing not implemented")
  }

  def refreshIndex = {
    Http.runTask(http.post(uri"/${name}/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
  }

  def runSearch(doc: JValue): Option[JValue] = try {
    Some(Http.runTask(http.post(uri"/${name}/${mappingType}/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }

  def updateBulk(jsonLines: Seq[JValue], refreshIndex: Boolean = false): (Boolean, JValue) = {
    val url = if (refreshIndex) uri"/${name}/_bulk?refresh=wait_for" else uri"/${name}/_bulk"
    val response: JValue = Http.runTask(http.post(url, jsonLines)(Json4sHttp4s.multiLineJson4sEncoderOf[JValue])(Http.parseJson[JValue]))
    (extract[Boolean](response \ "errors"), response)
  }

  }
}
