package fi.oph.koski.elasticsearch

import com.typesafe.config.Config
import fi.oph.koski.db.BackgroundExecutionContext
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.{Json4sHttp4s, JsonDiff}
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import org.json4s.jackson.JsonMethods
import org.json4s._
import rx.lang.scala.Observable
import scala.concurrent.Future

class ElasticSearchIndex(
  val elastic: ElasticSearch,
  val config: Config,
  val name: String,
  val mappingType: String,
  val mapping: JValue,
  val settings: JValue
) extends Logging with BackgroundExecutionContext {
  protected def http: Http = elastic.http

  lazy val init: Future[Any] = {
    val indexChanged = if (indexExists) {
      migrateIndex
    } else {
      createIndex
    }

    val reindexingNeeded = indexChanged || config.getBoolean("elasticsearch.reIndexAtStartup")
    Future {
      if (reindexingNeeded) {
        reindex
      }
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
    // TODO: Pitäisi myös käsitellä mappingin muuttuminen
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
    Http.runTask(http.put(uri"/${name}/_mapping/${mappingType}", mapping)(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
    true
  }

  protected def reindex: Observable[Any] = {
    throw new NotImplementedError("Reindexing not implemented")
  }

  def refreshIndex = {
    Http.runTask(http.post(uri"/${name}/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
  }

  def runSearch(query: JValue): Option[JValue] = try {
    Some(Http.runTask(http.post(uri"/${name}/${mappingType}/_search", query)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
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

  def deleteAll: Unit = {
    val query: JValue = JObject("query" -> JObject("match_all" -> JObject()))
    val deleted = deleteByQuery(query)
    logger.info(s"Deleted ($deleted) documents")
  }

  def deleteByQuery(query: JValue): Int = {
    val deletedCount = Http.runTask(http.post(uri"/${name}/${mappingType}/_delete_by_query", query)(Json4sHttp4s.json4sEncoderOf[JValue]) {
      case (200, text, request) => extract[Int](JsonMethods.parse(text) \ "deleted")
      case (status, text, request) if List(404, 409).contains(status) => 0
      case (status, text, request) => throw HttpStatusException(status, text, request)
    })
    deletedCount
  }

  def analyze(string: String): List[String] = {
    val query = JObject("analyzer" -> JString("default"), "text" -> JString(string))
    val response: JValue = Http.runTask(
      http.post(uri"/${name}/_analyze", query)
      (Json4sHttp4s.json4sEncoderOf[JObject])(Http.parseJson[JValue])
    )
    val tokens: List[JValue] = extract[List[JValue]](response \ "tokens")
    tokens.map(token => extract[String](token \ "token"))
  }
}
