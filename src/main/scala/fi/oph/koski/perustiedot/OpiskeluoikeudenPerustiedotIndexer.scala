package fi.oph.koski.perustiedot

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.json.{Json, Json4sHttp4s}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.{PaginationSettings, Timing}
import org.http4s.EntityEncoder
import org.json4s._

import scala.concurrent.Future

object PerustiedotIndexUpdater extends App with Timing {
  val perustiedotIndexer = KoskiApplication.apply.perustiedotIndexer
  timed("Reindex") {
    perustiedotIndexer.reIndex(None).toBlocking.last
    println("done")
  }
}

class OpiskeluoikeudenPerustiedotIndexer(config: Config, index: PerustiedotSearchIndex, opiskeluoikeusQueryService: OpiskeluoikeusQueryService) extends Logging with GlobalExecutionContext {
  import PerustiedotSearchIndex._

  lazy val init = {
    val reIndexingNeeded = setupIndex

    if (reIndexingNeeded || config.getBoolean("elasticsearch.reIndexAtStartup")) {
      Future {
        reIndex() // Re-index on background
      }
    }
  }

  /**
    * Update (replace) or insert info to Elasticsearch. Return error status or a boolean indicating whether data was changed.
    */
  def update(perustiedot: OpiskeluoikeudenPerustiedot): Either[HttpStatus, Int] = updateBulk(List(perustiedot), replaceDocument = true)

  /*
   * Update or insert info to Elasticsearch. Return error status or a boolean indicating whether data was changed.
   *
   * if replaceDocument is true, this will replace the whole document. If false, only the supplied data fields will be updated.
   */
  def updateBulk(items: Seq[WithId], replaceDocument: Boolean): Either[HttpStatus, Int] = {
    if (items.isEmpty) {
      return Right(0)
    }
    val jsonLines: Seq[Map[String, Any]] = items.flatMap { perustiedot =>
      List(
        Map("update" -> Map("_id" -> perustiedot.id, "_index" -> "koski", "_type" -> "perustiedot")),
        Map("doc_as_upsert" -> replaceDocument, "doc" -> perustiedot)
      )
    }
    val response = Http.runTask(index.elasticSearchHttp.post(uri"/koski/_bulk", jsonLines)(Json4sHttp4s.multiLineJson4sEncoderOf[Map[String, Any]])(Http.parseJson[JValue]))
    val errors = (response \ "errors").extract[Boolean]
    if (errors) {
      val msg = s"Elasticsearch indexing failed for some of ids ${items.map(_.id)}: ${Json.writePretty(response)}"
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = (response \ "items").extract[List[JValue]].map(_ \ "update" \ "_shards" \ "successful").map(_.extract[Int])
      Right(itemResults.sum)
    }
  }

  def deleteByOppijaOids(oids: List[Oid]) = {
    val doc = Json.toJValue(Map("query" -> Map("bool" -> Map("should" -> Map("terms" -> Map("henkilö.oid" -> oids))))))

    val deleted = Http.runTask(index.elasticSearchHttp
      .post(uri"/koski/perustiedot/_delete_by_query", doc)(Json4sHttp4s.json4sEncoderOf[JValue]) {
        case (200, text, request) => (Json.parse(text) \ "deleted").extract[Int]
        case (status, text, request) if List(404, 409).contains(status) => 0
        case (status, text, request) => throw HttpStatusException(status, text, request)
      })
    deleted
  }

  def reIndex(pagination: Option[PaginationSettings] = None) = {
    logger.info("Starting elasticsearch re-indexing")
    val bufferSize = 1000
    val observable = opiskeluoikeusQueryService.streamingQuery(Nil, None, pagination)(KoskiSession.systemUser).tumblingBuffer(bufferSize).zipWithIndex.map {
      case (rows, index) =>
        val perustiedot = rows.par.map { case (opiskeluoikeusRow, henkilöRow) =>
          OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow)
        }.toList
        val changed = updateBulk(perustiedot, replaceDocument = true) match {
          case Right(count) => count
          case Left(_) => 0 // error already logged
        }
        UpdateStatus(rows.length, changed)
    }.scan(UpdateStatus(0, 0))(_ + _)


    observable.subscribe({case UpdateStatus(countSoFar, actuallyChanged) => logger.info(s"Updated elasticsearch index for ${countSoFar} rows, actually changed ${actuallyChanged}")},
      {e: Throwable => logger.error(e)("Error updating Elasticsearch index")},
      { () => logger.info("Finished updating Elasticsearch index")})
    observable
  }

  private def setupIndex: Boolean = {
    val settings = Json.parse("""
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

    val mappings = Map("perustiedot" -> Map("properties" -> Map(
      "tilat" -> Map("type" -> "nested"),
      "suoritukset" -> Map("type" -> "nested")
    ))) // TODO: check if mappings changed, force re-creation of index

    val statusCode = Http.runTask(index.elasticSearchHttp.get(uri"/koski")(Http.statusCode))
    var reindexNeeded = if (indexExists) {
      val serverSettings = (Http.runTask(index.elasticSearchHttp.get(uri"/koski/_settings")(Http.parseJson[JValue])) \ "koski-index" \ "settings" \ "index").extract[Map[String, Any]]
      val alreadyApplied = (serverSettings ++ settings) == serverSettings
      if (alreadyApplied) {
        logger.info("Elasticsearch index settings are up to date")
        false
      } else {
        logger.info("Updating Elasticsearch index settings")
        Http.runTask(index.elasticSearchHttp.post(uri"/koski/_close", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        Http.runTask(index.elasticSearchHttp.put(uri"/koski/_settings", Json.toJValue(settings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
        Http.runTask(index.elasticSearchHttp.post(uri"/koski/_open", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
        logger.info("Updated Elasticsearch index settings. Re-indexing is needed.")
        true
      }
    } else {
      logger.info("Creating Elasticsearch index")
      Http.runTask(index.elasticSearchHttp.put(uri"/koski-index", Json.toJValue(Map("settings" -> settings, "mappings" -> mappings)))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Creating Elasticsearch index alias")
      Http.runTask(index.elasticSearchHttp.post(uri"/_aliases", Json.toJValue(Map("actions" -> List(Map("add" -> Map("index" -> "koski-index", "alias" -> "koski"))))))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))
      logger.info("Created index and alias.")
      true
    }

    reindexNeeded
  }

  private def indexExists = {
    Http.runTask(index.elasticSearchHttp.get(uri"/koski")(Http.statusCode)) match {
      case 200 => true
      case 404 => false
      case statusCode =>
        throw new RuntimeException("Unexpected status code from elasticsearch: " + statusCode)
    }
  }

  case class UpdateStatus(updated: Int, changed: Int) {
    def +(other: UpdateStatus) = UpdateStatus(this.updated + other.updated, this.changed + other.changed)
  }
}
