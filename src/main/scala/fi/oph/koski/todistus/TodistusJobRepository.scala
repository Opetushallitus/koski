package fi.oph.koski.todistus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

class TodistusJobRepository(val db: DB, workerId: String) extends QueryMethods with Logging with DatabaseConverters {

  def get(id: String)(implicit user: KoskiSpecificSession): Option[TodistusJob] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus_job
      WHERE id = ${id}::uuid
        AND (user_oid = ${user.oid} OR ${user.hasRole(OPHPAAKAYTTAJA)})
      """.as[TodistusJob]
    ).headOption
  }

  def add(todistusJob: TodistusJob)(implicit user: KoskiSpecificSession): Option[TodistusJob] = {
    if (todistusJob.userOid == user.oid || user.hasRole(OPHPAAKAYTTAJA)) {
      runDbSync(sql"""
      INSERT INTO todistus_job(id, user_oid, oppija_oid, opiskeluoikeus_oid, language,
                          opiskeluoikeus_versionumero, oppija_henkilotiedot_hash, state,
                          created_at, started_at, completed_at, worker, error)
      VALUES (
        ${todistusJob.id}::uuid,
        ${todistusJob.userOid},
        ${todistusJob.oppijaOid},
        ${todistusJob.opiskeluoikeusOid},
        ${todistusJob.language},
        ${todistusJob.opiskeluoikeusVersionumero},
        ${todistusJob.oppijaHenkilötiedotHash},
        ${todistusJob.state},
        ${java.sql.Timestamp.valueOf(todistusJob.createdAt)},
        ${todistusJob.startedAt.map(java.sql.Timestamp.valueOf)},
        ${todistusJob.completedAt.map(java.sql.Timestamp.valueOf)},
        ${todistusJob.worker},
        ${todistusJob.error}
      )
      RETURNING *
      """.as[TodistusJob]).headOption
    } else {
      logger.warn(s"Käyttäjä ${user.oid} yritti lisätä todistuksen oppijalle ${todistusJob.oppijaOid} (käyttäjä: ${todistusJob.userOid}) ilman oikeuksia")
      None
    }
  }

  def updateState(id: String, startState: String, state: String): Option[TodistusJob] = {
    runDbSync(sql"""
      UPDATE todistus_job
      SET state = $state
      WHERE id = ${id}::uuid AND
        state = $startState
      RETURNING *
      """.as[TodistusJob]).headOption
  }

  def takeNext: Option[TodistusJob] = {
    runDbSync(sql"""
      UPDATE todistus_job
      SET
        state = 'GENERATING_RAW_PDF',
        worker = $workerId,
        started_at = now()
      WHERE id IN (
        SELECT id
        FROM todistus_job
        WHERE state = 'QUEUED'
        ORDER BY created_at
        LIMIT 1
      )
      RETURNING *
      """.as[TodistusJob]).headOption
  }

  def setRunningJobsFailed(error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET
        state = ${TodistusState.ERROR},
        error = $error,
        completed_at = now()
      WHERE worker = $workerId
        AND state = any(${TodistusState.runningStates.toSeq})
      """.asUpdate) != 0

  def numberOfQueuedJobs: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM todistus_job
      WHERE state = ${TodistusState.QUEUED}
      """.as[Int]).head

  def numberOfMyRunningJobs: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM todistus_job
      WHERE state = any(${TodistusState.runningStates.toSeq})
        AND worker = $workerId
      """.as[Int]).head

  def findOrphanedJobs(koskiInstances: Seq[String]): Seq[TodistusJob] =
    runDbSync(
      sql"""
      SELECT *
      FROM todistus_job
      WHERE state = any(${TodistusState.runningStates.toSeq})
        AND NOT worker = any($koskiInstances)
      """.as[TodistusJob])

  def truncate: Int = runDbSync(sql"TRUNCATE TABLE todistus_job".asUpdate)

  implicit private val getTodistusJobResult: GetResult[TodistusJob] = GetResult[TodistusJob](r => {
    TodistusJob(
      id = r.rs.getString("id"),
      userOid = r.rs.getString("user_oid"),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      language = r.rs.getString("language"),
      opiskeluoikeusVersionumero = Option(r.rs.getInt("opiskeluoikeus_versionumero")),
      oppijaHenkilötiedotHash = Option(r.rs.getString("oppija_henkilotiedot_hash")),
      state = r.rs.getString("state"),
      createdAt = r.rs.getTimestamp("created_at").toLocalDateTime,
      startedAt = Option(r.rs.getTimestamp("started_at")).map(_.toLocalDateTime),
      completedAt = Option(r.rs.getTimestamp("completed_at")).map(_.toLocalDateTime),
      worker = Option(r.rs.getString("worker")),
      error = Option(r.rs.getString("error"))
    )
  })
}
