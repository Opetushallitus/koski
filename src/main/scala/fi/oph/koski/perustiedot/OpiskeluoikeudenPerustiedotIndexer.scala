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
import org.json4s._

import scala.concurrent.Future

object PerustiedotIndexUpdater extends App with Timing {
  val perustiedotIndexer = KoskiApplication.apply.perustiedotIndexer
  timed("Reindex") {
    perustiedotIndexer.reIndex(None).toBlocking.last
    println("done")
  }
}

class OpiskeluoikeudenPerustiedotIndexer(config: Config, index: KoskiElasticSearchIndex, opiskeluoikeusQueryService: OpiskeluoikeusQueryService) extends Logging with GlobalExecutionContext {
  import PerustiedotSearchIndex._

  lazy val init = {
    index.init

    val mappings = Map("perustiedot" -> Map("properties" -> Map(
      "tilat" -> Map("type" -> "nested"),
      "suoritukset" -> Map("type" -> "nested")
    )))

    Http.runTask(index.http.put(uri"/koski-index/_mapping/perustiedot", Json.toJValue(mappings))(Json4sHttp4s.json4sEncoderOf)(Http.parseJson[JValue]))

    if (index.reindexingNeededAtStartup || config.getBoolean("elasticsearch.reIndexAtStartup")) {
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
  def updateBulk(items: Seq[Oidillinen], replaceDocument: Boolean): Either[HttpStatus, Int] = {
    if (items.isEmpty) {
      return Right(0)
    }
    val jsonLines: Seq[Map[String, Any]] = items.flatMap { perustiedot =>
      List(
        Map("update" -> Map("_id" -> perustiedot.oid, "_index" -> "koski", "_type" -> "perustiedot")),
        Map("doc_as_upsert" -> replaceDocument, "doc" -> perustiedot)
      )
    }
    val response = Http.runTask(index.http.post(uri"/koski/_bulk", jsonLines)(Json4sHttp4s.multiLineJson4sEncoderOf[Map[String, Any]])(Http.parseJson[JValue]))
    val errors = (response \ "errors").extract[Boolean]
    if (errors) {
      val msg = s"Elasticsearch indexing failed for some of ids ${items.map(_.oid)}: ${Json.writePretty(response)}"
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = (response \ "items").extract[List[JValue]].map(_ \ "update" \ "_shards" \ "successful").map(_.extract[Int])
      Right(itemResults.sum)
    }
  }

  def deleteByOppijaOids(oids: List[Oid]) = {
    val doc = Json.toJValue(Map("query" -> Map("bool" -> Map("should" -> Map("terms" -> Map("henkilö.oid" -> oids))))))

    val deleted = Http.runTask(index.http
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
    val observable = opiskeluoikeusQueryService.opiskeluoikeusQuery(Nil, None, pagination)(KoskiSession.systemUser).tumblingBuffer(bufferSize).zipWithIndex.map {
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

  case class UpdateStatus(updated: Int, changed: Int) {
    def +(other: UpdateStatus) = UpdateStatus(this.updated + other.updated, this.changed + other.changed)
  }
}
