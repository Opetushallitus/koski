package fi.oph.koski.ytr.download

import fi.oph.koski.db.{DB, DatabaseExecutionContext, KoskiTables, QueryMethods, YtrDownloadStatusRow}
import fi.oph.koski.log.Logging
import org.json4s.{DefaultFormats, JValue}
import org.json4s.jackson.JsonMethods

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import slick.jdbc.GetResult

class YtrDownloadStatus(val db: DB) extends QueryMethods with Logging with DatabaseExecutionContext{
  implicit val formats = DefaultFormats

  def init() = initLoading().map(_.id).getOrElse(throw new InternalError("Ytr-latauksen statuksen initialisointi epäonnistui."))

  def latestIsLoading: Boolean = getDownloadStatusLatest == "loading"
  def latestIsComplete: Boolean = getDownloadStatusLatest == "complete"
  def isComplete(id: Int): Boolean = getDownloadStatus(id) == "complete"
  def initLoading() = setStatus(id = 0, "initialized", 0, 0)
  def setLoading(id: Int, totalCount: Int, errorCount: Int = 0, modifiedSinceParam: Option[LocalDate] = None) = setStatus(id, "loading", totalCount, errorCount, modifiedSince = modifiedSinceParam)
  def setComplete(id: Int, totalCount: Int, errorCount: Int = 0, completedAt: LocalDateTime = LocalDateTime.now()): Option[YtrDownloadStatusRow] = setStatus(id, "complete", totalCount, errorCount, completedAt = Some(completedAt))
  def setError(id: Int, totalCount: Int, errorCount: Int = 0) = setStatus(id, "error", totalCount, errorCount)

  def lastCompletedRun(): Option[Timestamp] = {
    runDbSync(KoskiTables.YtrDownloadStatus
      .filter(r => r.completed != null && r.modifiedSinceParam != null)
      .sortBy(row => (row.initialized.desc)).result)
      .headOption
      .map(_.aikaleima)
  }

  private def getDownloadStatus(id: Int): String = {
    (getDownloadStatusJson(id) \ "current" \ "status").extract[String]
  }

  def getDownloadStatusLatest: String = {
    val rawJson = getDownloadStatusJsonLatest()
    (rawJson \ "current" \ "status").extract[String]
  }

  def getDownloadStatusRows(): Seq[YtrDownloadStatusRow] = {
    runDbSync(KoskiTables.YtrDownloadStatus.sortBy(_.aikaleima).result)
  }

  def getDownloadStatusJson(id: Int): JValue = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.id === id).result).headOption.map(_.data)
      .getOrElse(constructStatusJson("idle", None, 0, 0))
  }

  def getDownloadStatusJsonLatest(): JValue = {
    runDbSync(KoskiTables.YtrDownloadStatus.sortBy(_.aikaleima.desc).result).headOption.map(_.data)
      .getOrElse(constructStatusJson("idle", None, 0, 0))
  }

  private def rowById(id: Int): Option[YtrDownloadStatusRow] = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.id === id).result).headOption
  }
  private def setStatus(id: Int, currentStatus: String, totalCount: Int, errorCount: Int = 0, completedAt: Option[LocalDateTime] = None, modifiedSince: Option[LocalDate] = None) = {
    val initializedAt = rowById(id).map(_.initialized).getOrElse(Timestamp.valueOf(LocalDateTime.now))
    val modifiedSinceParam: Option[LocalDate] = rowById(id).flatMap(_.modifiedSinceParam).orElse(modifiedSince)
    val maybeCompletedAt: Option[Timestamp] = completedAt.map(Timestamp.valueOf)

    runDbSync(KoskiTables.YtrDownloadStatus
      .returning(KoskiTables.YtrDownloadStatus)
      .insertOrUpdate(
        YtrDownloadStatusRow(
          id,
          aikaleima = Timestamp.valueOf(LocalDateTime.now),
          constructStatusJson(currentStatus, Some(LocalDateTime.now), totalCount, errorCount),
          initializedAt,
          maybeCompletedAt,
          modifiedSinceParam
        )
      )
    )
  }

  private def constructStatusJson(currentStatus: String, timestamp: Option[LocalDateTime], totalCount: Int, errorCount: Int): JValue = {
    val timestampPart = timestamp.map(Timestamp.valueOf).map(t =>
      s"""
         |, "timestamp": "${t.toString}"
         |""".stripMargin).getOrElse("")

    JsonMethods.parse(s"""
                         | {
                         |   "current": {
                         |     "status": "${currentStatus}",
                         |     "totalCount": ${totalCount},
                         |     "errorCount": ${errorCount}
                         |     ${timestampPart}
                         |   }
                         | }""".stripMargin
    )
  }
}
