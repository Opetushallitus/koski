package fi.oph.koski.opensearch

import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatusException}
import fi.oph.koski.json.Json4sHttp4s
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.log.Logging
import org.http4s.EntityEncoder
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.concurrent.duration.DurationInt

class OpenSearchIndex(
                          val name: String,
                          private val openSearch: OpenSearch,
                          private val mapping: Map[String, Any],
                          private val mappingVersion: Int,
                          private val settings: Map[String, Any],
                          private val initialLoader: () => Unit
) extends Logging {

  private def http: Http = openSearch.http

  private val currentIndexName: String = versionedIndexName(mappingVersion)

  private val readAlias: String = s"$name-read-alias"

  private val writeAlias: String = s"$name-write-alias"

  lazy val init: Unit = {
    if (indexExists(currentIndexName)) {
      logger.info(s"OpenSearch index $name exists")
      if (settingsChanged()) {
        throw new IllegalArgumentException(s"Index $name settings changed but mapping version not updated")
        // Mapping version should be bumped when settings are changed so that we know to reindex
      }
      ensureAliases(mappingVersion)
    } else {
      val previousIndexName = versionedIndexName(mappingVersion - 1)
      if (indexExists(previousIndexName)) {
        logger.warn(s"Index $previousIndexName is at previous version and needs reindexing!")
        ensureAliases(mappingVersion - 1)
      } else {
        logger.info(s"No previous $name index found - creating an index from scratch")
        createIndex(mappingVersion)
        migrateAlias(readAlias, mappingVersion)
        migrateAlias(writeAlias, mappingVersion)
      }
    }
  }

  private def versionedIndexName(version: Int): String = s"$name-v$version"

  private def migrateAlias(aliasName: String, toVersion: Int, fromVersion: Option[Int] = None): String = {
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
    Http.runIO(http.post(uri"/_aliases", toJValue(query))(Json4sHttp4s.json4sEncoderOf[JValue]) {
      case (200, _text, _request) => aliasName
      case (status, text, request) => throw HttpStatusException(status, text, request)
    })
  }

  def migrateReadAlias(toVersion: Int, fromVersion: Option[Int] = None): String = migrateAlias(readAlias, toVersion, fromVersion)

  def migrateWriteAlias(toVersion: Int, fromVersion: Option[Int] = None): String = migrateAlias(writeAlias, toVersion, fromVersion)

  def isOnline: Boolean = indexExists(currentIndexName)

  private def indexExists(indexName: String): Boolean = {
    Http.runIO(http.head(uri"/$indexName", timeout = 5.seconds)(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode: Int => throw new RuntimeException("Unexpected status code from opensearch: " + statusCode)
    }
  }

  private def aliasExists(aliasName: String): Boolean = {
    Http.runIO(http.head(uri"/_alias/$aliasName")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode: Int => throw new RuntimeException("Unexpected status code from opensearch: " + statusCode)
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
    val response = Http.runIO(http.get(uri"/$currentIndexName/_settings")(Http.parseJson[JValue]))
    val serverSettings = response \ currentIndexName \ "settings" \ "index"
    val mergedSettings = serverSettings.merge(toJValue(settings))
    val alreadyApplied = mergedSettings == serverSettings
    if (alreadyApplied) {
      logger.info(s"OpenSearch index $name settings are up to date")
      false
    } else {
      logger.info(s"OpenSearch index $name settings need updating")
      true
    }
  }

  def createIndex(version: Int): String = {
    val indexName = versionedIndexName(version)
    logger.info(s"Creating OpenSearch index $indexName")
    Http.runIO(http.put(uri"/$indexName", toJValue(Map(
      "settings" -> settings,
      "mappings" -> mapping
    )))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
    indexName
  }

  def reindex(fromVersion: Int, toVersion: Int): (String, String) = {
    val fromIndex = versionedIndexName(fromVersion)
    val toIndex = versionedIndexName(toVersion)
    logger.info(s"Reindexing from $fromIndex to $toIndex. This will take a while if the index is large.")
    val query = Map(
      "source" -> Map(
        "index" -> fromIndex
      ),
      "dest" -> Map(
        "index" -> toIndex,
        "op_type" -> "create"
      ),
      "conflicts" -> "proceed"
    )
    Http.runIO(http.post(uri"/_reindex?wait_for_completion=false&refresh", toJValue(query))(Json4sHttp4s.json4sEncoderOf)(Http.statusCode)) match {
      case 200 => Unit
      case statusCode: Int => throw new RuntimeException(s"Reindexing failed with $statusCode. Response: ")
    }
    logger.info(s"Started reindexing from $fromIndex to $toIndex")
    (fromIndex, toIndex)
  }

  def reload(): Unit = {
    initialLoader()
    refreshIndex()
  }

  private def refreshIndex(): Unit = {
    Http.runIO(http.post(uri"/$readAlias,$writeAlias/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
  }

  def runSearch(query: JValue): Option[JValue] = {
    Some(Http.runIO(http.post(uri"/$readAlias/_search", query)(Json4sHttp4s.json4sEncoderOf[JValue])(Http.parseJson[JValue])))
  }

  def updateBulk(docsAndIds: Seq[(JValue, String)], upsert: Boolean, refresh: Boolean): (Boolean, JValue) = {
    val queries = docsAndIds
      .flatMap { case (doc, id) => buildUpdateQuery(doc, id, upsert) }
      .map(q => toJValue(q))
    val url = uri"/$writeAlias/_bulk?refresh=${refresh}"
    val response: JValue = Http.runIO(http.post(url, queries)(Json4sHttp4s.multiLineJson4sEncoderOf[JValue])(Http.parseJson[JValue]))
    (extract[Boolean](response \ "errors"), response)
  }

  private def buildUpdateQuery(doc: JValue, id: String, upsert: Boolean): Seq[Map[String, Any]] = {
    List(
      Map(
        "update" -> Map(
          "_id" -> id,
          "_index" -> writeAlias
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
    val deleted = deleteByQuery(query, refresh = true)
    logger.info(s"Deleted $deleted documents from $name")
  }

  def deleteByQuery(query: JValue, refresh: Boolean): Int = {
    val deletedCount = Http.runIO(http.post(uri"/$writeAlias/_delete_by_query?refresh=${refresh}", query)(Json4sHttp4s.json4sEncoderOf[JValue]) {
      case (200, text, request) => extract[Int](JsonMethods.parse(text) \ "deleted")
      case (status, text, request) if List(404, 409).contains(status) => 0
      case (status, text, request) => throw HttpStatusException(status, text, request)
    })
    deletedCount
  }

  def analyze(string: String): List[String] = {
    val query = JObject("analyzer" -> JString("default"), "text" -> JString(string))
    val response: JValue = Http.runIO(
      http.post(uri"/$readAlias/_analyze", query)
      (Json4sHttp4s.json4sEncoderOf[JObject])(Http.parseJson[JValue])
    )
    val tokens: List[JValue] = extract[List[JValue]](response \ "tokens")
    tokens.map(token => extract[String](token \ "token"))
  }
}
