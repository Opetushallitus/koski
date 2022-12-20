package fi.oph.koski.perustiedot

import fi.oph.koski.opensearch.{OpenSearch, OpenSearchIndex}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.LegacyJsonSerialization.toJValue
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot.docId
import fi.oph.koski.schema.Henkilö._
import fi.oph.koski.util.DateOrdering
import fi.oph.koski.util.SortOrder.Descending
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.util.{Failure, Success, Try}

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
                                          openSearch: OpenSearch,
                                          opiskeluoikeusQueryService: OpiskeluoikeusQueryService,
                                          perustiedotSyncRepository: PerustiedotSyncRepository,
                                          perustiedotManualSyncRepository: PerustiedotManualSyncRepository
) extends Logging {

  val index = new OpenSearchIndex(
    openSearch = openSearch,
    name = "perustiedot",
    mappingVersion = 3,
    mapping = OpiskeluoikeudenPerustiedotIndexer.mapping,
    settings = OpiskeluoikeudenPerustiedotIndexer.settings,
    initialLoader = this.indexAllDocuments
  )

  def init(): Unit = {
    index.init
  }

  def statistics(): OpiskeluoikeudenPerustiedotStatistics = OpiskeluoikeudenPerustiedotStatistics(index)

  def sync(refresh: Boolean): Unit = synchronized {
    logger.debug("Checking for sync rows")
    val allRows = perustiedotSyncRepository.queuedUpdates(1000)
    val rowsToBeSync = allRows.groupBy(_.opiskeluoikeusId).mapValues(_.sortBy(_.aikaleima)(DateOrdering.ascedingSqlTimestampOrdering).last).map(_._2).toSeq
    if (allRows.nonEmpty) {
      logger.debug(s"Syncing ${allRows.length} rows")
      val (itemsToDelete, itemsToUpdate) = rowsToBeSync.partition(r => r.data == JObject(List.empty))

      itemsToUpdate.groupBy(_.upsert) foreach { case (upsert, syncRows) =>
        updatePerustiedotRaw(syncRows.map(_.data), upsert, refresh)
      }
      deletePerustiedot(itemsToDelete.map(_.opiskeluoikeusId).toList)
      perustiedotSyncRepository.deleteFromQueue(allRows.map(_.id))
      logger.debug(s"Done syncing ${allRows.length} rows")
    }
  }

  /**
   * manualSync-funktiota käytetään OpenSearch-indeksin manuaaliseen päivittämiseen.
   * @param refresh
   */
  def manualSync(refresh: Boolean): Unit = synchronized {
    logger.debug("Checking for manual sync rows")
    val allRows = perustiedotManualSyncRepository.getQueuedUpdates(1000)
    val itemsToSync = allRows.groupBy(_._2.opiskeluoikeusOid).mapValues(_.sortBy(_._2.aikaleima)(DateOrdering.ascedingSqlTimestampOrdering).last).map(_._2).toSeq
    if (itemsToSync.nonEmpty) {
      logger.debug(s"Manually syncing ${itemsToSync.length} rows")
      itemsToSync.groupBy(_._2.upsert) foreach { case (upsert, syncRows) =>
        perustiedotSyncRepository.addToSyncQueueRaw(syncRows.map({
          case ((ooRow, henkRow), _) => perustiedotManualSyncRepository.makeSyncRow(ooRow, henkRow)
        }).flatten, upsert)
      }
      perustiedotManualSyncRepository.deleteFromQueue(allRows.map(_._2.id))
      logger.debug(s"Done manually syncing ${allRows.length} rows")
    }
  }

  def updatePerustiedot(items: Seq[OpiskeluoikeudenOsittaisetTiedot], upsert: Boolean, refresh: Boolean): Either[HttpStatus, Int] = {
    updatePerustiedotRaw(items.map(OpiskeluoikeudenPerustiedot.serializePerustiedot), upsert, refresh)
  }

  private def updatePerustiedotRaw(items: Seq[JValue], upsert: Boolean, refresh: Boolean): Either[HttpStatus, Int] = {
    if (items.isEmpty) {
      return Right(0)
    }
    val docsAndIds = items.flatMap(generateUpdate)
    val (errors, response) = index.updateBulk(docsAndIds, upsert, refresh)
    if (errors) {
      val failedOpiskeluoikeusIds: List[Int] = extract[List[JValue]](response \ "items" \ "update")
        .flatMap { item =>
          if (item \ "error" != JNothing) List(extract[Int](item \ "_id")) else Nil
        }
      val toSyncAgain = failedOpiskeluoikeusIds.flatMap { id =>
        items.find{ doc => docId(doc) == id}.orElse{
          logger.warn(s"OpenSearch reported failed id $id that was not found in ${items.map(docId)}");
          None
        }
      }

      perustiedotSyncRepository.addToSyncQueueRaw(toSyncAgain, upsert) // FIXME: Sivuvaikutuksena timestamppi päivittyy, kun rivi lisätään uudestaan jonoon. Oikea korjaus olisi käyttää opiskeluoikeuden versionumeroa.

      val msg = s"""OpenSearch indexing failed for ids ${failedOpiskeluoikeusIds.mkString(", ")}.
Response from ES: ${JsonMethods.pretty(response)}.
Will retry soon."""
      logger.error(msg)
      Left(KoskiErrorCategory.internalError(msg))
    } else {
      val itemResults = extract[List[JValue]](response \ "items")
        .map(_ \ "update" \ "_shards" \ "successful")
        .map(extract[Int](_))
      Right(itemResults.sum)
    }
  }

  private def deletePerustiedot(itemIds: List[Int]): Either[HttpStatus, Int] = {
    if (itemIds.isEmpty) {
      return Right(0)
    }

    Try(deleteByIds(itemIds, true)) match {
      case Success(i) => Right(i)
      case Failure(err) =>
        perustiedotSyncRepository.addDeletesToSyncQueue(itemIds)
        val msg =
          s"""
              OpenSearch indexing failed for ids ${itemIds.mkString(", ")}.
              Exception from ES: ${err.getMessage}.
              Will retry soon.
              """
        logger.error(msg)
        Left(KoskiErrorCategory.internalError(msg))
    }
  }

  private def generateUpdate(perustiedot: JValue): Option[(JValue, String)] = {
    val doc = perustiedot.asInstanceOf[JObject] match {
      case JObject(fields) => JObject(
        fields.filter {
          case ("henkilö", JNull) => false // to prevent removing these from OpenSearch
          case ("henkilöOid", JNull) => false
          case _ => true
        }
      )
    }
    doc \ "id" match {
      case JInt(id) => Some((doc, id.toString()))
      case _ =>
        logger.error("Document id is not a number")
        None
    }
  }

  def deleteByOppijaOids(oids: List[Oid], refresh: Boolean): Int = {
    val query: JValue = toJValue(Map(
      "query" -> Map(
        "bool" -> Map(
          "should" -> Map(
            "terms" -> Map(
              "henkilö.oid" -> oids))))))
    index.deleteByQuery(query, refresh)
  }

  def deleteByIds(ids: List[Int], refresh: Boolean): Int = {
    val query: JValue = toJValue(Map(
      "query" -> Map(
        "bool" -> Map(
          "should" -> Map(
            "terms" -> Map(
              "id" -> ids))))))
    index.deleteByQuery(query, refresh)
  }

  private def indexAllDocuments() = {
    logger.info("Start indexing all perustiedot documents")
    val bufferSize = 100
    val observable = opiskeluoikeusQueryService
      .opiskeluoikeusQuery(Nil, Some(Descending("id")), None)(KoskiSpecificSession.systemUser)
      .tumblingBuffer(bufferSize)
      .zipWithIndex
      .map {
        case (rows, index) =>
          val perustiedot = rows.par.map { case (opiskeluoikeusRow, henkilöRow, masterHenkilöRow) =>
            OpiskeluoikeudenPerustiedot.makePerustiedot(opiskeluoikeusRow, henkilöRow, masterHenkilöRow)
          }.toList
          val changed = updatePerustiedot(perustiedot, upsert = true, refresh = false) match {
            case Right(count) => count
            case Left(_) => 0 // error already logged
          }
          UpdateStatus(rows.length, changed)
      }.scan(UpdateStatus(0, 0))(_ + _)

    observable.subscribe(
      {
        case UpdateStatus(countSoFar, actuallyChanged) => if (countSoFar > 0) {
          logger.info(s"Updated OpenSearch index for ${countSoFar} rows, actually changed ${actuallyChanged}")
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

  val SmallIndexMaxRows: Int = 500

  def indexIsLarge: Boolean = {
    statistics().statistics.opiskeluoikeuksienMäärä > SmallIndexMaxRows
  }
}
