package fi.oph.koski.ytr.download

import fi.oph.koski.db.{DB, KoskiTables, QueryMethods, YtrDownloadStatusRow}
import fi.oph.koski.log.Logging
import org.json4s.{DefaultFormats, JValue}
import org.json4s.jackson.JsonMethods

import java.sql.Timestamp
import java.time.LocalDateTime

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.json4s._

class YtrDownloadStatus(val db: DB) extends QueryMethods with Logging {
  implicit val formats = DefaultFormats

  private val tietokantaStatusRivinNimi = "ytr_download"

  def isLoading: Boolean = getDownloadStatus == "loading"
  def isComplete: Boolean = getDownloadStatus == "complete"
  def setLoading(totalCount: Int, errorCount: Int = 0) = setStatus("loading", totalCount, errorCount)
  def setComplete(totalCount: Int, errorCount: Int = 0) = setStatus("complete", totalCount, errorCount)
  def setError(totalCount: Int, errorCount: Int = 0) = setStatus("error", totalCount, errorCount)

  private def getDownloadStatus: String = {
    (getDownloadStatusJson \ "current" \ "status").extract[String]
  }

  def getDownloadStatusJson: JValue = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.nimi === tietokantaStatusRivinNimi).result).headOption.map(_.data)
      .getOrElse(constructStatusJson("idle", None, 0, 0))
  }

  private def setStatus(currentStatus: String, totalCount: Int, errorCount: Int = 0) = {
    runDbSync(KoskiTables.YtrDownloadStatus.insertOrUpdate(
      YtrDownloadStatusRow(
        tietokantaStatusRivinNimi,
        Timestamp.valueOf(LocalDateTime.now),
        constructStatusJson(currentStatus, Some(LocalDateTime.now), totalCount, errorCount)
      )
    ))
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
