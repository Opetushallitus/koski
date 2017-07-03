package fi.oph.koski.perustiedot
import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.http.Http._
import fi.oph.koski.json.{Json, Json4sHttp4s}
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import org.json4s.{JValue, _}

class KoskiElasticSearchIndex(elastic: ElasticSearch) extends Logging {
  import PerustiedotSearchIndex.formats

  def http = elastic.http
  def reindexingNeededAtStartup = init

  lazy val init = {
    if (indexExists) {
      logger.info("ElasticSearch index exists")
      val serverSettings = (Http.runTask(http.get(uri"/koski/_settings")(Http.parseJson[JValue])) \ "koski-index" \ "settings" \ "index").extract[Map[String, Any]]
      val alreadyApplied = (serverSettings ++ settings) == serverSettings
      if (alreadyApplied) {
        logger.info("Elasticsearch index settings are up to date")
        false
      } else {
        logger.info("Updating Elasticsearch index settings")
        Http.runTask(http.post(uri"/koski/_close", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        Http.runTask(http.put(uri"/koski/_settings", Json.toJValue(settings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
        Http.runTask(http.post(uri"/koski/_open", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        logger.info("Updated Elasticsearch index settings. Re-indexing is needed.")
        true
      }
    } else {
      logger.info("Creating Elasticsearch index")
      Http.runTask(http.put(uri"/koski-index", Json.toJValue(Map("settings" -> settings)))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Creating Elasticsearch index alias")
      Http.runTask(http.post(uri"/_aliases", Json.toJValue(Map("actions" -> List(Map("add" -> Map("index" -> "koski-index", "alias" -> "koski"))))))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Created index and alias.")
      true
    }
  }

  def runSearch(tyep: String, doc: JValue): Option[JValue] = try {
    Some(Http.runTask(http.post(uri"/koski/${tyep}/_search", doc)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }


  private def indexExists = {
    Http.runTask(http.get(uri"/koski")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode =>
        throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  private def settings = Json.parse("""
    {
        "analysis": {
          "filter": {
            "finnish_folding": {
              "type": "icu_folding",
              "unicodeSetFilter": "[^åäöÅÄÖ]"
            }
          },
          "analyzer": {
            "default": {
              "tokenizer": "icu_tokenizer",
              "filter":  [ "finnish_folding", "lowercase" ]
            }
          }
        }
    }""").extract[Map[String, Any]]
}
