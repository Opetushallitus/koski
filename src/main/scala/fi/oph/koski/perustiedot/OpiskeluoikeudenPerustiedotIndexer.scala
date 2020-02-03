package fi.oph.koski.perustiedot

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.BackgroundExecutionContext
import fi.oph.koski.elasticsearch.{ElasticSearch, ElasticSearchIndex}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.docId
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.Timing
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.concurrent.Future

object PerustiedotIndexUpdater extends App with Timing {
  val perustiedotIndexer = KoskiApplication.apply.perustiedotIndexer
  timed("Reindex") {
    perustiedotIndexer.indexAllDocuments.toBlocking.last
  }
}

object OpiskeluoikeudenPerustiedotIndexer {
  private val settings = JsonMethods.parse("""
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
    }""")

  private val mapping = toJValue(Map("properties" -> Map(
    "tilat" -> Map("type" -> "nested"),
    "suoritukset" -> Map("type" -> "nested")
  )))
}

class OpiskeluoikeudenPerustiedotIndexer(
  config: Config,
  elastic: ElasticSearch,
  opiskeluoikeusQueryService: OpiskeluoikeusQueryService,
  perustiedotSyncRepository: PerustiedotSyncRepository
) extends ElasticSearchIndex(
  config = config,
  elastic = elastic,
  name = "koski-index",
  mappingType = "perustiedot",
  mapping = OpiskeluoikeudenPerustiedotIndexer.mapping,
  settings = OpiskeluoikeudenPerustiedotIndexer.settings
) with BackgroundExecutionContext {

  def updatePerustiedot(items: Seq[OpiskeluoikeudenOsittaisetTiedot], upsert: Boolean): Either[HttpStatus, Int] = {
    val serializedItems = items.map(OpiskeluoikeudenPerustiedot.serializePerustiedot)
    if (serializedItems.isEmpty) {
      return Right(0)
    }
    val (errors, response) = updateBulk(generateJson(serializedItems, upsert))
    if (errors) {
      val failedOpiskeluoikeusIds: List[Int] = extract[List[JValue]](response \ "items" \ "update")
        .flatMap { item =>
          if (item \ "error" != JNothing) List(extract[Int](item \ "_id")) else Nil
        }
      perustiedotSyncRepository.syncAgain(failedOpiskeluoikeusIds.flatMap { id =>
        serializedItems.find{doc => docId(doc) == id}.orElse{
          logger.warn(s"Elasticsearch reported failed id $id that was not found in ${serializedItems.map(docId)}");
          None
        }
      }, upsert)
      val msg = s"Elasticsearch indexing failed for ids $failedOpiskeluoikeusIds: ${JsonMethods.pretty(response)}. Will retry soon."
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = extract[List[JValue]](response \ "items")
        .map(_ \ "update" \ "_shards" \ "successful")
        .map(extract[Int](_))
      Right(itemResults.sum)
    }
  }

  private def generateJson(serializedItems: Seq[JValue], upsert: Boolean) = {
    serializedItems.flatMap { (perustiedot: JValue) =>
      val doc = perustiedot.asInstanceOf[JObject] match {
        case JObject(fields) => JObject(
          fields.filter {
            case ("henkilö", JNull) => false // to prevent removing these from ElasticSearch
            case ("henkilöOid", JNull) => false
            case _ => true
          }
        )
      }

      List(
        JObject("update" -> JObject("_id" -> (doc \ "id"), "_index" -> JString("koski"), "_type" -> JString("perustiedot"))),
        JObject("doc_as_upsert" -> JBool(upsert), "doc" -> doc)
      )
    }
  }

  def deleteByOppijaOids(oids: List[Oid]) = {
    val query: JValue = toJValue(Map(
      "query" -> Map(
        "bool" -> Map(
          "should" -> Map(
            "terms" -> Map(
              "henkilö.oid" -> List(oids)))))))
    deleteByQuery(query)
  }

  override protected def reindex: Unit = {
    Future {
      indexAllDocuments
    }
  }

  def indexAllDocuments = {
    logger.info("Indexing all perustiedot documents")
    val bufferSize = 100
    val observable = opiskeluoikeusQueryService
      .opiskeluoikeusQuery(Nil, None, None)(KoskiSession.systemUser)
      .tumblingBuffer(bufferSize)
      .zipWithIndex
      .map {
        case (rows, index) =>
          val perustiedot = rows.par.map { case (opiskeluoikeusRow, henkilöRow, masterHenkilöRow) =>
            OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow, masterHenkilöRow)
          }.toList
          val changed = updatePerustiedot(perustiedot, upsert = true) match {
            case Right(count) => count
            case Left(_) => 0 // error already logged
          }
          UpdateStatus(rows.length, changed)
      }.scan(UpdateStatus(0, 0))(_ + _)

    observable.subscribe(
      {
        case UpdateStatus(countSoFar, actuallyChanged) => if (countSoFar > 0) {
          logger.info(s"Updated elasticsearch index for ${countSoFar} rows, actually changed ${actuallyChanged}")
        }
      },
      { e: Throwable => logger.error(e)("Error while indexing perustiedot documents") },
      { () => logger.info("Indexed all perustiedot documents") })
    observable
  }

  case class UpdateStatus(updated: Int, changed: Int) {
    def +(other: UpdateStatus) = {
      UpdateStatus(this.updated + other.updated, this.changed + other.changed)
    }
  }

  def indexIsLarge: Boolean = {
    OpiskeluoikeudenPerustiedotStatistics(this).statistics.opiskeluoikeuksienMäärä > 100
  }
}
