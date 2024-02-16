package fi.oph.koski.kyselyt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import slick.jdbc.GetResult

import java.time.LocalDateTime
import java.util.UUID

class KyselyRepository(
  val db: DB,
  workerId: String,
  extractor: ValidatingAndResolvingExtractor,
)  extends QueryMethods with Logging  {

  def get(id: UUID)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM kysely
      WHERE id = ${id.toString}::uuid
        AND user_oid = ${user.oid}
      """.as[Query]
    ).headOption

  def getExisting(query: QueryParameters)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM kysely
      WHERE requested_by = ${user.oid}
        AND query = ${query.asJson}
        AND state IN (${QueryState.pending}, ${QueryState.running})
     """.as[Query]
    ).headOption

  def add(query: QueryParameters)(implicit user: KoskiSpecificSession): PendingQuery =
    runDbSync(sql"""
      INSERT INTO kysely(id, requested_by, query, state)
      VALUES (
        ${UUID.randomUUID().toString}::uuid,
        ${user.oid},
        ${query.asJson},
        ${QueryState.pending}
       )
       RETURNING *
       """.as[Query])
      .collectFirst { case q: PendingQuery => q }
      .get

  def numberOfRunningQueries: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM kysely
      WHERE state = ${QueryState.running}
        AND worker = $workerId
      """.as[Int]).head

  def takeNext: Option[RunningQuery] =
    runDbSync(sql"""
      UPDATE kysely
      SET
        state = ${QueryState.running},
        worker = $workerId,
        work_start_time = now()
      WHERE id IN (
        SELECT id
        FROM kysely
        WHERE state = ${QueryState.pending}
        ORDER BY creation_time
        LIMIT 1
      )
      RETURNING *
      """.as[Query])
      .collectFirst { case q: RunningQuery => q }

  def setComplete(id: String, resultFiles: List[String]): Boolean =
    runDbSync(sql"""
      UPDATE kysely
      SET
        state = ${QueryState.complete},
        result_files = ${resultFiles},
        end_time = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setFailed(id: String, error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE kysely
      SET
        state = ${QueryState.failed},
        error = $error,
        end_time = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setRunningTasksFailed(error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE kysely
      SET
        state = ${QueryState.failed},
        error = $error,
        end_time = now()
      WHERE worker = $workerId
        AND state = ${QueryState.running}
      """.asUpdate) != 0

  implicit private val getQueryResult: GetResult[Query] = GetResult[Query] { r =>
    val id = r.rs.getString("id")
    val requestedBy = r.rs.getString("requested_by")
    val query = parseParameters(r.rs.getString("query"))
    val creationTime = r.rs.getTimestamp("creation_time").toLocalDateTime

    r.rs.getString("state") match {
      case QueryState.pending => PendingQuery(
        queryId = id,
        requestedBy = requestedBy,
        query = query,
        creationTime = creationTime,
      )
      case QueryState.running => RunningQuery(
        queryId = id,
        requestedBy = requestedBy,
        query = query,
        creationTime = creationTime,
        workStartTime = r.rs.getTimestamp("work_start_time").toLocalDateTime,
        worker = r.rs.getString("worker")
      )
      case QueryState.complete => CompleteQuery(
        queryId = id,
        requestedBy = requestedBy,
        query = query,
        creationTime = creationTime,
        workStartTime = r.rs.getTimestamp("work_start_time").toLocalDateTime,
        endTime = r.rs.getTimestamp("end_time").toLocalDateTime,
        worker = r.rs.getString("worker"),
        resultFiles = r.getArray("result_files").toList,
      )
      case QueryState.failed => FailedQuery(
        queryId = id,
        requestedBy = requestedBy,
        query = query,
        creationTime = creationTime,
        workStartTime = r.rs.getTimestamp("work_start_time").toLocalDateTime,
        endTime = r.rs.getTimestamp("end_time").toLocalDateTime,
        worker = r.rs.getString("worker"),
        error = r.rs.getString("error"),
      )
    }
  }

  private def parseParameters(parameters: String): QueryParameters = {
    val json = JsonMethods.parse(parameters)
    extractor.extract[QueryParameters](strictDeserialization)(json).right.get // TODO: parempi virheenhallinta siltä varalta että parametrit eivät deserialisoidukaan
  }
}

trait Query {
  def queryId: String
  def requestedBy: String
  def query: QueryParameters
  def state: String
  def creationTime: LocalDateTime
}
case class PendingQuery(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  creationTime: LocalDateTime,
) extends Query {
  def state: String = QueryState.pending
}

case class RunningQuery(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  creationTime: LocalDateTime,
  workStartTime: LocalDateTime,
  worker: String,
) extends Query {
  def state: String = QueryState.running
}

case class CompleteQuery(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  creationTime: LocalDateTime,
  workStartTime: LocalDateTime,
  endTime: LocalDateTime,
  worker: String,
  resultFiles: List[String],
) extends Query {
    def state: String = QueryState.complete
}

case class FailedQuery(
  queryId: String,
  requestedBy: String,
  query: QueryParameters,
  creationTime: LocalDateTime,
  workStartTime: LocalDateTime,
  endTime: LocalDateTime,
  worker: String,
  error: String
) extends Query {
    def state: String = QueryState.failed
}

object QueryState {
  val pending = "pending"
  val running = "running"
  val complete = "complete"
  val failed = "failed"
  val * : Set[String] = Set(pending, running, complete, failed)
}
