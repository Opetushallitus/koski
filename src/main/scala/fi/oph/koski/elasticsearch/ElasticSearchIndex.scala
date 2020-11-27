package fi.oph.koski.elasticsearch

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.Json4sHttp4s
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import org.json4s._
import org.json4s.jackson.JsonMethods

class ElasticSearchIndex(
  private val elastic: ElasticSearch,
  private val name: String,
  private val legacyName: String,
  private val mapping: Map[String, Any],
  private val mappingVersion: Int,
  private val settings: Map[String, Any]
) extends Logging {

  private def http: Http = elastic.http

  private val currentIndexName: String = versionedIndexName(mappingVersion)

  private val readAlias: String = s"$name-read-alias"

  private val writeAlias: String = s"$name-write-alias"

  // TODO: Hankkiudu eroon erillisestä mapping-tyypistä - tuki poistuu uusissa ES-versioissa
  private val mappingTypeName = name

  lazy val init: Unit = {
    if (indexExists(currentIndexName)) {
      logger.info(s"ElasticSearch index $name exists")
      if (settingsChanged()) {
        throw new IllegalArgumentException(s"Index $name settings changed but mapping version not updated")
        // Mapping version should be bumped when settings are changed so that we know to reindex
      }
      ensureAliases(mappingVersion)
    } else {
      val previousIndexName = versionedIndexName(mappingVersion - 1)
      if (indexExists(previousIndexName)) {
        logger.info(s"Index $previousIndexName is at previous version and needs reindexing")
        reindexFromPreviousVersion()
      } else {
        logger.info(s"No previous $name index found - creating an index from scratch")
        createIndex(mappingVersion)
        migrateAlias(readAlias, mappingVersion)
        migrateAlias(writeAlias, mappingVersion)
      }
    }
  }

  private def versionedIndexName(version: Int): String = {
    if (version == 1) {
      // TODO: Poista erillinen käsittely tälle tapaukselle kun indeksit on reindeksoitu uusille nimille
      legacyName
    } else {
      s"$name-v$version"
    }
  }

  private def migrateAlias(aliasName: String, toVersion: Int, fromVersion: Option[Int] = None): Unit = {
    val removeOp = fromVersion match {
      case Some(version) =>
        val fromIndex = versionedIndexName(version)
        if (indexExists(fromIndex) && aliasExists(aliasName)) {
          logger.info(s"Migrating $aliasName from v$version to v$toVersion")
          Some(Map("remove" -> Map("index" -> fromIndex, "alias" -> aliasName)))
        } else {
          throw new RuntimeException(s"Attempted to remove $aliasName from $fromIndex but index or alias does not exist")
        }
      case None =>
        logger.info(s"Creating $aliasName to v$toVersion")
        None
    }

    val addOp = Map("add" -> Map("index" -> versionedIndexName(toVersion), "alias" -> aliasName))

    val ops = addOp :: removeOp.toList
    val query = Map("actions" -> ops)
    Http.runTask(http.post(uri"/_aliases", toJValue(query))(Json4sHttp4s.json4sEncoderOf[JValue]) {
      case (200, _text, _request) => Unit
      case (status, text, request) => throw HttpStatusException(status, text, request)
    })
  }

  private def indexExists(indexName: String): Boolean = {
    Http.runTask(http.head(uri"/$indexName")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode: Int => throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  private def aliasExists(aliasName: String): Boolean = {
    Http.runTask(http.head(uri"/_alias/$aliasName")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode: Int => throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  private def ensureAliases(indexVersion: Int): Unit = {
    List(readAlias, writeAlias).foreach(aliasName =>
      if (!aliasExists(aliasName)) {
        logger.warn(s"Alias $aliasName missing for index $name")
        migrateAlias(aliasName, indexVersion)
      }
    )
  }

  private def settingsChanged(): Boolean = {
    val response = Http.runTask(http.get(uri"/$currentIndexName/_settings")(Http.parseJson[JValue]))
    val serverSettings = response \ currentIndexName \ "settings" \ "index"
    val mergedSettings = serverSettings.merge(toJValue(settings))
    val alreadyApplied = mergedSettings == serverSettings
    if (alreadyApplied) {
      logger.info(s"Elasticsearch index $name settings are up to date")
      false
    } else {
      logger.info(s"Elasticsearch index $name settings need updating")
      true
    }
  }

  private def createIndex(version: Int): Unit = {
    val indexName = versionedIndexName(version)
    logger.info(s"Creating Elasticsearch index $indexName")
    Http.runTask(http.put(uri"/$indexName", JObject("settings" -> toJValue(settings)))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
    Http.runTask(http.put(uri"/$indexName/_mapping/$mappingTypeName", toJValue(mapping))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
  }

  private def reindexFromPreviousVersion(): Unit = {
    ensureAliases(mappingVersion - 1)
    createIndex(mappingVersion)
    migrateAlias(writeAlias, mappingVersion, Some(mappingVersion - 1))
    reindex(mappingVersion - 1, mappingVersion)
    migrateAlias(readAlias, mappingVersion, Some(mappingVersion - 1))
  }

  private def reindex(fromVersion: Int, toVersion: Int): Unit = {
    val fromIndex = versionedIndexName(fromVersion)
    val toIndex = versionedIndexName(toVersion)
    logger.info(s"Reindexing from $fromIndex to $toIndex. This will take a while if the index is large.")
    val query = Map(
      "source" -> Map(
        "index" -> fromIndex
      ),
      "dest" -> Map(
        "index" -> toIndex,
        "version_type" -> "external"
      )
    )
    Http.runTask(http.post(uri"/_reindex?wait_for_completion=true&refresh", toJValue(query))(Json4sHttp4s.json4sEncoderOf)(Http.statusCode)) match {
      case 200 => Unit
      case statusCode: Int => throw new RuntimeException(s"Reindexing failed with $statusCode. Response: ")
    }
    logger.info(s"Done reindexing from $fromIndex to $toIndex")
  }

  def refreshIndex(): Unit = {
    Http.runTask(http.post(uri"/$readAlias/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
  }

  def runSearch(query: JValue): Option[JValue] = try {
    Some(Http.runTask(http.post(uri"/$readAlias/$mappingTypeName/_search", query)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  } catch {
    case e: HttpStatusException if e.status == 400 =>
      logger.warn(e.getMessage)
      None
  }

  def updateBulk(docsAndIds: Seq[(JValue, String)], upsert: Boolean): (Boolean, JValue) = {
    val queries = docsAndIds
      .flatMap { case (doc, id) => buildUpdateQuery(doc, id, upsert) }
      .map(q => toJValue(q))
    val url = uri"/$writeAlias/_bulk"
    val response: JValue = Http.runTask(http.post(url, queries)(Json4sHttp4s.multiLineJson4sEncoderOf[JValue])(Http.parseJson[JValue]))
    (extract[Boolean](response \ "errors"), response)
  }

  private def buildUpdateQuery(doc: JValue, id: String, upsert: Boolean): Seq[Map[String, Any]] = {
    List(
      Map(
        "update" -> Map(
          "_id" -> id,
          "_index" -> writeAlias,
          "_type" -> mappingTypeName
        )
      ),
      Map(
        "doc_as_upsert" -> upsert,
        "doc" -> doc
      )
    )
  }

  def deleteAll(): Unit = {
    val query: JValue = JObject("query" -> JObject("match_all" -> JObject()))
    val deleted = deleteByQuery(query)
    logger.info(s"Deleted $deleted documents from $name")
  }

  def deleteByQuery(query: JValue): Int = {
    val deletedCount = Http.runTask(http.post(uri"/$writeAlias/$mappingTypeName/_delete_by_query", query)(Json4sHttp4s.json4sEncoderOf[JValue]) {
      case (200, text, request) => extract[Int](JsonMethods.parse(text) \ "deleted")
      case (status, text, request) if List(404, 409).contains(status) => 0
      case (status, text, request) => throw HttpStatusException(status, text, request)
    })
    deletedCount
  }

  def analyze(string: String): List[String] = {
    val query = JObject("analyzer" -> JString("default"), "text" -> JString(string))
    val response: JValue = Http.runTask(
      http.post(uri"/$readAlias/_analyze", query)
      (Json4sHttp4s.json4sEncoderOf[JObject])(Http.parseJson[JValue])
    )
    val tokens: List[JValue] = extract[List[JValue]](response \ "tokens")
    tokens.map(token => extract[String](token \ "token"))
  }
}
