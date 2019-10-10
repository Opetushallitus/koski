package fi.oph.koski.perustiedot

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.BackgroundExecutionContext
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.json.Json4sHttp4s
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.docId
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.{PaginationSettings, Timing}
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.concurrent.Future

object PerustiedotIndexUpdater extends App with Timing {
  val perustiedotIndexer = KoskiApplication.apply.perustiedotIndexer
  timed("Reindex") {
    perustiedotIndexer.reIndex(filters = Nil, pagination = None).toBlocking.last
  }
}

class OpiskeluoikeudenPerustiedotIndexer(config: Config, index: KoskiElasticSearchIndex, opiskeluoikeusQueryService: OpiskeluoikeusQueryService, perustiedotSyncRepository: PerustiedotSyncRepository) extends Logging with BackgroundExecutionContext {
  lazy val init = {
    index.init

    val mappings: JValue = JObject("perustiedot" -> JObject("properties" -> JObject(
      "tilat" -> JObject("type" -> JString("nested")),
      "suoritukset" -> JObject("type" -> JString("nested")),
      "luokka" -> JObject(
        "type" -> JString("text"),
        "analyzer" -> JString("lowercase_whitespace_analyzer"),
        "fielddata" -> JBool(true),
        "fields" -> JObject(
          "keyword" -> JObject(
            "type" -> JString("keyword")
          )
        )
      )
    )))

    Http.runTask(index.http.put(uri"/koski-index/_mapping/perustiedot", mappings)(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))

    val reindexingNeeded = index.reindexingNeededAtStartup || config.getBoolean("elasticsearch.reIndexAtStartup")
    Future {
      if (reindexingNeeded) {
          reIndex() // Re-index on background
      }
    }
  }

  /*
   * Update or insert info to Elasticsearch. Return error status or a boolean indicating whether data was changed.
   *
   * if replaceDocument is true, this will replace the whole document. If false, only the supplied data fields will be updated.
   */

  /*
   * Update or insert info to Elasticsearch. Return error status or a boolean indicating whether data was changed.
   *
   * if replaceDocument is true, this will replace the whole document. If false, only the supplied data fields will be updated.
   */
  def updateBulk(items: Seq[OpiskeluoikeudenOsittaisetTiedot], upsert: Boolean): Either[HttpStatus, Int] = {
    updateBulkRaw(items.map(OpiskeluoikeudenPerustiedot.serializePerustiedot), upsert)
  }

  def updateBulkRaw(items: Seq[JValue], upsert: Boolean): Either[HttpStatus, Int] = {
    // TODO: päättele upsertti riveittäin

    if (items.isEmpty) {
      return Right(0)
    }
    val (errors, response) = index.updateBulk(items.flatMap { (perustiedot: JValue) =>

      val doc = perustiedot.asInstanceOf[JObject] match {
        case JObject(fields) => JObject(
          fields.filter {
            case ("henkilö", JNull) => false // to prevent removing these from ElasticSearch. TODO: not a nice way to do it, improve! Probably should have a separate class for Perustiedot without henkilö/henkilöOid fields, so that nulls wouldn't be there.
            case ("henkilöOid", JNull) => false
            case _ => true
          }
        )
      }

      List(
        JObject("update" -> JObject("_id" -> (doc \ "id"), "_index" -> JString("koski"), "_type" -> JString("perustiedot"))),
        JObject("doc_as_upsert" -> JBool(upsert), "doc" -> doc)
      )
    })

    if (errors) {
      val failedOpiskeluoikeusIds: List[Int] = extract[List[JValue]](response \ "items" \ "update") .flatMap { item =>
        if (item \ "error" != JNothing) List(extract[Int](item \ "_id")) else Nil
      }
      perustiedotSyncRepository.syncAgain(failedOpiskeluoikeusIds.flatMap { id =>
        items.find{doc => docId(doc) == id}.orElse{logger.warn(s"Elasticsearch reported failed id $id that was not found in ${items.map(docId)}"); None}
      }, upsert)
      val msg = s"Elasticsearch indexing failed for ids $failedOpiskeluoikeusIds: ${JsonMethods.pretty(response)}. Will retry soon."
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = extract[List[JValue]](response \ "items").map(_ \ "update" \ "_shards" \ "successful").map(extract[Int](_))
      Right(itemResults.sum)
    }
  }

  def deleteByOppijaOids(oids: List[Oid]) = {
    val doc: JValue = JObject("query" -> JObject("bool" -> JObject("should" -> JObject("terms" -> JObject("henkilö.oid" -> JArray(oids.map(JString)))))))

    import org.json4s.jackson.JsonMethods.parse

    val deleted = Http.runTask(index.http
      .post(uri"/koski/perustiedot/_delete_by_query", doc)(Json4sHttp4s.json4sEncoderOf[JValue]) {
        case (200, text, request) => extract[Int](parse(text) \ "deleted")
        case (status, text, request) if List(404, 409).contains(status) => 0
        case (status, text, request) => throw HttpStatusException(status, text, request)
      })
    deleted
  }

  def reIndex(filters: List[OpiskeluoikeusQueryFilter] = Nil, pagination: Option[PaginationSettings] = None) = {
    logger.info("Starting elasticsearch re-indexing")
    val bufferSize = 100
    val observable = opiskeluoikeusQueryService.opiskeluoikeusQuery(filters, None, pagination)(KoskiSession.systemUser).tumblingBuffer(bufferSize).zipWithIndex.map {
      case (rows, index) =>
        val perustiedot = rows.par.map { case (opiskeluoikeusRow, henkilöRow, masterHenkilöRow) =>
          OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow, masterHenkilöRow)
        }.toList
        val changed = updateBulk(perustiedot, true) match {
          case Right(count) => count
          case Left(_) => 0 // error already logged
        }
        UpdateStatus(rows.length, changed)
    }.scan(UpdateStatus(0, 0))(_ + _)


    observable.subscribe({case UpdateStatus(countSoFar, actuallyChanged) => if (countSoFar > 0) logger.info(s"Updated elasticsearch index for ${countSoFar} rows, actually changed ${actuallyChanged}")},
      {e: Throwable => logger.error(e)("Error updating Elasticsearch index")},
      { () => logger.info("Finished updating Elasticsearch index")})
    observable
  }

  case class UpdateStatus(updated: Int, changed: Int) {
    def +(other: UpdateStatus) = UpdateStatus(this.updated + other.updated, this.changed + other.changed)
  }
}
