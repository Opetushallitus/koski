package fi.oph.koski.perustiedot

import com.typesafe.config.Config
import fi.oph.koski.elasticsearch.{ElasticSearch, ElasticSearchIndex}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.docId
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.SortOrder.Descending
import org.json4s._
import org.json4s.jackson.JsonMethods

object OpiskeluoikeudenPerustiedotIndexer {
  private val settings = Map(
    "analysis" -> Map(
      "filter" -> Map(
        "finnish_folding" -> Map(
          "type" -> "icu_folding",
          "unicodeSetFilter" -> "[^åäöÅÄÖ]"
        )
      ),
      "analyzer" -> Map(
        "default" -> Map(
          "tokenizer" -> "icu_tokenizer",
          "filter" -> Array("finnish_folding", "lowercase")
        )
      ),
      "normalizer" -> Map(
        "keyword_lowercase" -> Map(
          "type" -> "custom",
          "filter" -> Array("lowercase")
        )
      )
    )
  )

  private val finnishSortedTextField = Map(
    "type" -> "text",
    "fields" -> Map(
      "keyword" -> Map(
        "type" -> "keyword"
      ),
      "sort" -> Map(
        "type" -> "icu_collation_keyword",
        "language" -> "fi",
        "country" -> "FI"
      )
    )
  )

  private val mapping = Map(
    "properties" -> Map(
      "henkilö" -> Map(
        "properties" -> Map(
          "etunimet" -> finnishSortedTextField,
          "kutsumanimi" -> finnishSortedTextField,
          "sukunimi" -> finnishSortedTextField
        )
      ),
      "luokka" -> Map(
        "type" -> "text",
        "fields" -> Map(
          "keyword" -> Map(
            "type" -> "keyword",
            "normalizer" -> "keyword_lowercase"
          )
        )
      ),
      "tilat" -> Map("type" -> "nested"),
      "suoritukset" -> Map("type" -> "nested")
    )
  )
}

class OpiskeluoikeudenPerustiedotIndexer(
  elastic: ElasticSearch,
  opiskeluoikeusQueryService: OpiskeluoikeusQueryService,
  perustiedotSyncRepository: PerustiedotSyncRepository
) extends Logging {

  var index = new ElasticSearchIndex(
    elastic = elastic,
    name = "perustiedot",
    legacyName = "perustiedot",
    mappingVersion = 2,
    mapping = OpiskeluoikeudenPerustiedotIndexer.mapping,
    settings = OpiskeluoikeudenPerustiedotIndexer.settings,
    initialLoader = this.indexAllDocuments
  )

  def init(): Unit = {
    index.init
  }

  def statistics(): OpiskeluoikeudenPerustiedotStatistics = OpiskeluoikeudenPerustiedotStatistics(index)

  def updatePerustiedot(items: Seq[OpiskeluoikeudenOsittaisetTiedot], upsert: Boolean): Either[HttpStatus, Int] = {
    updatePerustiedotRaw(items.map(OpiskeluoikeudenPerustiedot.serializePerustiedot), upsert)
  }

  def updatePerustiedotRaw(items: Seq[JValue], upsert: Boolean): Either[HttpStatus, Int] = {
    if (items.isEmpty) {
      return Right(0)
    }
    val docsAndIds = generateUpdates(items)
    val (errors, response) = index.updateBulk(docsAndIds, upsert)
    if (errors) {
      val failedOpiskeluoikeusIds: List[Int] = extract[List[JValue]](response \ "items" \ "update")
        .flatMap { item =>
          if (item \ "error" != JNothing) List(extract[Int](item \ "_id")) else Nil
        }
      val toSyncAgain = failedOpiskeluoikeusIds.flatMap { id =>
        items.find{ doc => docId(doc) == id}.orElse{
          logger.warn(s"Elasticsearch reported failed id $id that was not found in ${items.map(docId)}");
          None
        }
      }
      perustiedotSyncRepository.syncAgain(toSyncAgain, upsert)
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

  private def generateUpdates(serializedItems: Seq[JValue]): Seq[(JValue, String)] = {
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
      doc \ "id" match {
        case JInt(id) => Some((doc, id.toString()))
        case _ => None
      }
    }
  }

  def deleteByOppijaOids(oids: List[Oid]): Int = {
    val query: JValue = toJValue(Map(
      "query" -> Map(
        "bool" -> Map(
          "should" -> Map(
            "terms" -> Map(
              "henkilö.oid" -> oids))))))
    index.deleteByQuery(query)
  }

  private def indexAllDocuments() = {
    logger.info("Start indexing all perustiedot documents")
    val bufferSize = 100
    val observable = opiskeluoikeusQueryService
      .opiskeluoikeusQuery(Nil, Some(Descending("id")), None)(KoskiSession.systemUser)
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
    statistics().statistics.opiskeluoikeuksienMäärä > 500
  }
}
