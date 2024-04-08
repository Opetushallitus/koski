package fi.oph.koski.massaluovutus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiMockUser, KoskiSpecificSession, KäyttöoikeusRepository, MockUser}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import slick.jdbc.GetResult

import java.net.InetAddress
import java.sql.Timestamp
import java.time.{Duration, LocalDateTime}
import java.util.UUID

class QueryRepository(
  val db: DB,
  workerId: String,
  extractor: ValidatingAndResolvingExtractor,
)  extends QueryMethods with Logging with DatabaseConverters  {

  def get(id: UUID)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM massaluovutus
      WHERE id = ${id.toString}::uuid
        AND user_oid = ${user.oid}
      """.as[Query]
    ).headOption

  def getExisting(query: MassaluovutusQueryParameters)(implicit user: KoskiSpecificSession): Option[Query] =
    runDbSync(sql"""
      SELECT *
      FROM massaluovutus
      WHERE user_oid = ${user.oid}
        AND query = ${query.asJson}
        AND state IN (${QueryState.pending}, ${QueryState.running})
     """.as[Query]
    ).headOption

  def add(query: MassaluovutusQueryParameters)(implicit user: KoskiSpecificSession): PendingQuery = {
    val session = JsonSerializer.serialize(StorableSession(user))
    runDbSync(sql"""
      INSERT INTO massaluovutus(id, user_oid, session, query, state)
      VALUES (
        ${UUID.randomUUID().toString}::uuid,
        ${user.oid},
        $session,
        ${query.asJson},
        ${QueryState.pending}
       )
       RETURNING *
       """.as[Query])
      .collectFirst { case q: PendingQuery => q }
      .get
  }

  def addRaw(query: Query): Query = {
    val createdAt = Timestamp.valueOf(query.createdAt)
    val startedAt = query match {
      case q: QueryWithStartTime => Some(Timestamp.valueOf(q.startedAt))
      case _ => None
    }
    val finishedAt = query match {
      case q: QueryWithFinishTime => Some(Timestamp.valueOf(q.finishedAt))
      case _ => None
    }
    val worker = query match {
      case q: QueryWithWorker => Some(q.worker)
      case _ => None
    }
    val resultFiles = query match {
      case q: CompleteQuery => Some(q.resultFiles)
      case _ => None
    }
    val error = query match {
      case q: FailedQuery => Some(q.error)
      case _ => None
    }
    val meta = query.meta.map(m => JsonSerializer.serializeWithRoot(m))

    runDbSync(sql"""
     INSERT INTO massaluovutus(id, user_oid, session, query, state, created_at, started_at, finished_at, worker, result_files, error, meta)
     VALUES(
        ${query.queryId}::uuid,
        ${query.userOid},
        ${query.session},
        ${query.query.asJson},
        ${query.state},
        $createdAt,
        $startedAt,
        $finishedAt,
        $worker,
        $resultFiles,
        $error,
        $meta
     )
     RETURNING *
     """.as[Query]).head
  }

  def numberOfRunningQueries: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM massaluovutus
      WHERE state = ${QueryState.running}
        AND worker = $workerId
      """.as[Int]).head

  def numberOfPendingQueries: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM massaluovutus
      WHERE state = ${QueryState.pending}
      """.as[Int]).head

  def takeNext: Option[RunningQuery] =
    runDbSync(sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.running},
        worker = $workerId,
        started_at = now()
      WHERE id IN (
        SELECT id
        FROM massaluovutus
        WHERE state = ${QueryState.pending}
        ORDER BY created_at
        LIMIT 1
      )
      RETURNING *
      """.as[Query])
      .collectFirst { case q: RunningQuery => q }

  def setComplete(id: String, resultFiles: List[String]): Boolean =
    runDbSync(sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.complete},
        result_files = ${resultFiles},
        finished_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setFailed(id: String, error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.failed},
        error = $error,
        finished_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def restart(query: Query, reason: String): Boolean = {
    val meta = JsonSerializer.serializeWithRoot(query.meta.getOrElse(QueryMeta()).withRestart(reason))
    runDbSync(
      sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.pending},
        started_at = NULL,
        meta = $meta
      WHERE id = ${query.queryId}::uuid
        AND state <> ${QueryState.pending}
      """.asUpdate) != 0
  }

  def setRunningTasksFailed(error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.failed},
        error = $error,
        finished_at = now()
      WHERE worker = $workerId
        AND state = ${QueryState.running}
      """.asUpdate) != 0

  def setLongRunningQueriesFailed(timeout: Duration, error: String): Seq[FailedQuery] = {
    val timeoutTime = Timestamp.valueOf(LocalDateTime.now().minus(timeout))
    runDbSync(
      sql"""
      UPDATE massaluovutus
      SET
        state = ${QueryState.failed},
        error = $error,
        finished_at = now()
      WHERE state = ${QueryState.running}
        AND started_at < $timeoutTime
      RETURNING *
      """.as[Query])
      .collect { case q: FailedQuery => q }
  }

  def findOrphanedQueries(koskiInstances: Seq[String]): Seq[RunningQuery] =
    runDbSync(
      sql"""
      SELECT *
      FROM massaluovutus
      WHERE state = ${QueryState.running}
        AND NOT worker = any($koskiInstances)
      """.as[Query])
      .collect { case q: RunningQuery => q }

  def patchMeta(id: String, meta: QueryMeta): QueryMeta = {
    val json = JsonSerializer.serializeWithRoot(meta)
    runDbSync(sql"""
      UPDATE massaluovutus
      SET meta = COALESCE(meta, '{}'::jsonb) || $json
      WHERE id = $id::uuid
      RETURNING meta
    """.as[QueryMeta]).head
  }

  implicit private val getQueryResult: GetResult[Query] = GetResult[Query] { r =>
    val id = r.rs.getString("id")
    val userOid = r.rs.getString("user_oid")
    val session = r.getJson("session")
    val query = parseParameters(r.getJson("query"))
    val creationTime = r.rs.getTimestamp("created_at").toLocalDateTime
    val meta = r.getNullableJson("meta").map(parseMeta)

    r.rs.getString("state") match {
      case QueryState.pending => PendingQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        meta = meta,
      )
      case QueryState.running => RunningQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        worker = r.rs.getString("worker"),
        meta = meta,
      )
      case QueryState.complete => CompleteQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        finishedAt = r.rs.getTimestamp("finished_at").toLocalDateTime,
        worker = r.rs.getString("worker"),
        resultFiles = r.getArray("result_files").toList,
        meta = meta,
      )
      case QueryState.failed => FailedQuery(
        queryId = id,
        userOid = userOid,
        session = session,
        query = query,
        createdAt = creationTime,
        startedAt = r.rs.getTimestamp("started_at").toLocalDateTime,
        finishedAt = r.rs.getTimestamp("finished_at").toLocalDateTime,
        worker = r.rs.getString("worker"),
        error = r.rs.getString("error"),
        meta = meta,
      )
    }
  }

  implicit private val getQueryMetaResult: GetResult[QueryMeta] = GetResult[QueryMeta] { r =>
    parseMeta(r.<<[JValue])
  }

  private def parseParameters(parameters: JValue): MassaluovutusQueryParameters =
    extractor.extract[MassaluovutusQueryParameters](strictDeserialization)(parameters).right.get // TODO: parempi virheenhallinta siltä varalta että parametrit eivät deserialisoidukaan

  private def parseMeta(meta: JValue): QueryMeta =
    extractor.extract[QueryMeta](strictDeserialization)(meta).right.get // TODO: parempi virheenhallinta siltä varalta että parametrit eivät deserialisoidukaan
}

trait Query {
  def queryId: String
  def userOid: String
  def query: MassaluovutusQueryParameters
  def state: String
  def createdAt: LocalDateTime
  def session: JValue
  def meta: Option[QueryMeta]

  def getSession(käyttöoikeudet: KäyttöoikeusRepository): Option[KoskiSpecificSession] =
    JsonSerializer
      .validateAndExtract[StorableSession](session)
      .map(_.toSession(käyttöoikeudet))
      .toOption

  def name: String = s"${query.getClass.getSimpleName}(${queryId})"

  def externalResultsUrl(rootUrl: String): String = MassaluovutusServletUrls.query(rootUrl, queryId)

  def restartCount: Int = meta.flatMap(_.restarts).map(_.size).getOrElse(0)
}

trait QueryWithStartTime {
  def startedAt: LocalDateTime
}

trait QueryWithFinishTime {
  def finishedAt: LocalDateTime
}

trait QueryWithWorker {
  def worker: String
}

case class PendingQuery(
  queryId: String,
  userOid: String,
  query: MassaluovutusQueryParameters,
  createdAt: LocalDateTime,
  session: JValue,
  meta: Option[QueryMeta],
) extends Query {
  def state: String = QueryState.pending
}

case class RunningQuery(
  queryId: String,
  userOid: String,
  query: MassaluovutusQueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  worker: String,
  session: JValue,
  meta: Option[QueryMeta],
) extends Query with QueryWithStartTime with QueryWithWorker {
  def state: String = QueryState.running
}

case class CompleteQuery(
  queryId: String,
  userOid: String,
  query: MassaluovutusQueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  finishedAt: LocalDateTime,
  worker: String,
  resultFiles: List[String],
  session: JValue,
  meta: Option[QueryMeta],
) extends Query with QueryWithStartTime with QueryWithFinishTime with QueryWithWorker  {
  def state: String = QueryState.complete

  def filesToExternal(rootUrl: String): List[String] = resultFiles.map(MassaluovutusServletUrls.file(rootUrl, queryId, _))
}

case class FailedQuery(
  queryId: String,
  userOid: String,
  query: MassaluovutusQueryParameters,
  createdAt: LocalDateTime,
  startedAt: LocalDateTime,
  finishedAt: LocalDateTime,
  worker: String,
  error: String,
  session: JValue,
  meta: Option[QueryMeta],
) extends Query with QueryWithStartTime with QueryWithFinishTime with QueryWithWorker  {
    def state: String = QueryState.failed
}

object QueryState {
  val pending = "pending"
  val running = "running"
  val complete = "complete"
  val failed = "failed"
  val * : Set[String] = Set(pending, running, complete, failed)
}

case class StorableSession(
  oid: String,
  username: String,
  name: String,
  lang: String,
  clientIp: String,
  userAgent: String,
) {
  def toSession(käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    val user = AuthenticationUser(
      oid = oid,
      username = username,
      name = name,
      serviceTicket = None,
    )
    new KoskiSpecificSession(
      user = AuthenticationUser(
        oid = oid,
        username = username,
        name = name,
        serviceTicket = None,
      ),
      lang = lang,
      clientIp = InetAddress.getByName(clientIp),
      userAgent = userAgent,
      lähdeKäyttöoikeudet = käyttöoikeudet.käyttäjänKäyttöoikeudet(user),
    )
  }

  def toJson: JValue = JsonSerializer.serializeWithRoot(this)
}

object StorableSession {
  def apply(session: KoskiSpecificSession): StorableSession = {
    StorableSession(
      oid = session.oid,
      username =  session.username,
      name = session.user.name,
      lang = session.lang,
      clientIp = session.clientIp.getHostAddress,
      userAgent = session.userAgent,
    )
  }

  def apply(user: MockUser): StorableSession = {
    StorableSession(
      oid = user.oid,
      username = user.username,
      name = user.username,
      lang = user.lang,
      clientIp = "127.0.0.1",
      userAgent = "Test",
    )
  }
}

case class QueryMeta(
  password: Option[String] = None,
  restarts: Option[List[String]] = None,
  raportointikantaGeneratedAt: Option[LocalDateTime] = None,
) {
  def withRestart(reason: String): QueryMeta = copy(
    restarts = Some(restarts.getOrElse(List.empty) :+ reason)
  )
}
