package fi.oph.koski.ytr.download

import fi.oph.koski.db.{DB, DatabaseExecutionContext, KoskiTables, QueryMethods, YtrDownloadStatusRow}
import fi.oph.koski.log.Logging
import org.json4s.{DefaultFormats, JValue}
import org.json4s.jackson.JsonMethods

import java.sql.Timestamp
import java.time.{LocalDateTime}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import slick.jdbc.GetResult

class YtrDownloadStatus(val db: DB) extends QueryMethods with Logging with DatabaseExecutionContext{
  implicit val formats = DefaultFormats

  def init() = {
    initLoading().map(_.id) match {
      case x: Some[Int] => x.get
      case _ => throw new InternalError("Ytr-latauksen statuksen initialisointi epäonnistui.")
    }
  }

  def latestIsLoading: Boolean = getDownloadStatusLatest == "loading"
  def latestIsComplete: Boolean = getDownloadStatusLatest == "complete"
  def isComplete(id: Int): Boolean = getDownloadStatus(id) == "complete"
  def initLoading() = setStatus(id = 0, "initialized", 0, 0)
  def setLoading(id: Int, totalCount: Int, errorCount: Int = 0) = setStatus(id, "loading", totalCount, errorCount)
  def setComplete(id: Int, totalCount: Int, errorCount: Int = 0, completedAt: LocalDateTime = LocalDateTime.now()): Option[YtrDownloadStatusRow] = setStatus(id, "complete", totalCount, errorCount, Some(completedAt))
  def setError(id: Int, totalCount: Int, errorCount: Int = 0) = setStatus(id, "error", totalCount, errorCount)

  def lastCompletedRun(): Option[Timestamp] = {
    runDbSync(KoskiTables.YtrDownloadStatus
      .filter(_.completed != null)
      .sortBy(row => (row.aikaleima.desc)).result)
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

  def getReplayLagSeconds: Int = {
    runDbSync(
      sql"""
        select extract(epoch from replay_lag) as replay_lag from pg_stat_replication;
      """.as[Double](GetResult(_.nextDouble))
    ).headOption.map(_.toInt).getOrElse(0)
  }

  private def aikaleimaById(id: Int) = {
    runDbSync(KoskiTables.YtrDownloadStatus.filter(_.id === id).result).headOption.map(_.aikaleima)
  }
  private def setStatus(id: Int, currentStatus: String, totalCount: Int, errorCount: Int = 0, completedAt: Option[LocalDateTime] = None) = {
    val aikaleima: Timestamp = aikaleimaById(id) match {
      case t: Some[Timestamp] => t.get
      case _ => Timestamp.valueOf(LocalDateTime.now)
    }

    val maybeCompletedAt: Option[Timestamp] = completedAt match {
      case dt: Some[LocalDateTime] => Some(Timestamp.valueOf(dt.get))
      case _ => None
    }

    runDbSync(KoskiTables.YtrDownloadStatus
      .returning(KoskiTables.YtrDownloadStatus)
      .insertOrUpdate(
        YtrDownloadStatusRow(
          id,
          aikaleima,
          constructStatusJson(currentStatus, Some(LocalDateTime.now), totalCount, errorCount),
          maybeCompletedAt
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
