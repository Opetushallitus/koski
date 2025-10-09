package fi.oph.koski.todistus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

import java.util.UUID

class TodistusRepository(val db: DB, workerId: String) extends QueryMethods with Logging with DatabaseConverters {

  def get(id: UUID)(implicit user: KoskiSpecificSession): Option[Todistus] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus
      WHERE id = ${id.toString}::uuid
        AND (user_oid = ${user.oid} OR ${user.hasRole(OPHPAAKAYTTAJA)})
      """.as[Todistus]
    ).headOption
  }

  def add(todistus: Todistus)(implicit user: KoskiSpecificSession): Option[Todistus] = {
    if (todistus.userOid == user.oid || user.hasRole(OPHPAAKAYTTAJA)) {
      runDbSync(sql"""
      INSERT INTO todistus(id, user_oid, oppija_oid, opiskeluoikeus_oid, language,
                          opiskeluoikeus_versionumero, oppija_henkilotiedot_hash, state,
                          created_at, started_at, completed_at, worker, raw_s3_object_key,
                          signed_s3_object_key, error)
      VALUES (
        ${todistus.id.toString}::uuid,
        ${todistus.userOid},
        ${todistus.oppijaOid},
        ${todistus.opiskeluoikeusOid},
        ${todistus.language},
        ${todistus.opiskeluoikeusVersionumero},
        ${todistus.oppijaHenkilotiedotHash},
        ${todistus.state},
        ${java.sql.Timestamp.valueOf(todistus.createdAt)},
        ${todistus.startedAt.map(java.sql.Timestamp.valueOf)},
        ${todistus.completedAt.map(java.sql.Timestamp.valueOf)},
        ${todistus.worker},
        ${todistus.rawS3ObjectKey},
        ${todistus.signedS3ObjectKey},
        ${todistus.error}
      )
      RETURNING *
      """.as[Todistus]).headOption
    } else {
      logger.warn(s"Käyttäjä ${user.oid} yritti lisätä todistuksen oppijalle ${todistus.oppijaOid} (käyttäjä: ${todistus.userOid}) ilman oikeuksia")
      None
    }
  }

  def updateState(id: UUID, startState: String, state: String): Option[Todistus] = {
    runDbSync(sql"""
      UPDATE todistus
      SET state = $state
      WHERE id = ${id.toString}::uuid AND
        state = $startState
      RETURNING *
      """.as[Todistus]).headOption
  }

  def takeNext: Option[Todistus] = {
    runDbSync(sql"""
      UPDATE todistus
      SET
        state = 'GENERATING_RAW_PDF',
        worker = $workerId,
        started_at = now()
      WHERE id IN (
        SELECT id
        FROM todistus
        WHERE state = 'QUEUED'
        ORDER BY created_at
        LIMIT 1
      )
      RETURNING *
      """.as[Todistus]).headOption
  }

  def setRunningTasksFailed(error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE todistus
      SET
        state = ${TodistusState.ERROR},
        error = $error,
        finished_at = now()
      WHERE worker = $workerId
        AND state = any(${TodistusState.runningStates.toSeq})
      """.asUpdate) != 0

  def numberOfQueuedTasks: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM todistus
      WHERE state = ${TodistusState.QUEUED}
      """.as[Int]).head

  def findOrphaned(koskiInstances: Seq[String]): Seq[Todistus] =
    runDbSync(
      sql"""
      SELECT *
      FROM todistus
      WHERE state = any(${TodistusState.runningStates.toSeq})
        AND NOT worker = any($koskiInstances)
      """.as[Todistus])

  implicit private val getTodistusResult: GetResult[Todistus] = GetResult[Todistus](r => {
    Todistus(
      id = UUID.fromString(r.rs.getString("id")),
      userOid = r.rs.getString("user_oid"),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      language = r.rs.getString("language"),
      opiskeluoikeusVersionumero = Option(r.rs.getInt("opiskeluoikeus_versionumero")),
      oppijaHenkilotiedotHash = Option(r.rs.getString("oppija_henkilotiedot_hash")),
      state = r.rs.getString("state"),
      createdAt = r.rs.getTimestamp("created_at").toLocalDateTime,
      startedAt = Option(r.rs.getTimestamp("started_at")).map(_.toLocalDateTime),
      completedAt = Option(r.rs.getTimestamp("completed_at")).map(_.toLocalDateTime),
      worker = Option(r.rs.getString("worker")),
      rawS3ObjectKey = Option(r.rs.getString("raw_s3_object_key")),
      signedS3ObjectKey = Option(r.rs.getString("signed_s3_object_key")),
      error = Option(r.rs.getString("error"))
    )
  })
}
