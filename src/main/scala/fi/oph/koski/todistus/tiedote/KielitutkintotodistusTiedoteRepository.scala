package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

import java.time.LocalDateTime

class KielitutkintotodistusTiedoteRepository(val db: DB, val workerId: String, config: Config) extends QueryMethods with Logging with DatabaseConverters {

  private val tableName = "kielitutkintotodistus_tiedote_job"

  def findNextEligible: Option[(String, String)] = {
    runDbSync(sql"""
      SELECT oo.oid, oo.oppija_oid
      FROM opiskeluoikeus oo
      WHERE oo.koulutusmuoto = 'kielitutkinto'
        AND oo.versionumero = 1
        AND NOT oo.mitatoity
        AND NOT oo.poistettu
        AND oo.data #>> '{suoritukset,0,vahvistus}' IS NOT NULL
        AND NOT EXISTS (
          SELECT 1 FROM kielitutkintotodistus_tiedote_job tj
          WHERE tj.opiskeluoikeus_oid = oo.oid
        )
      ORDER BY oo.aikaleima
      LIMIT 1
      """.as[(String, String)]
    ).headOption
  }

  def add(job: KielitutkintotodistusTiedoteJob): KielitutkintotodistusTiedoteJob = {
    runDbSync(sql"""
      INSERT INTO kielitutkintotodistus_tiedote_job(id, oppija_oid, opiskeluoikeus_oid, state, created_at, worker, attempts)
      VALUES (
        ${job.id}::uuid,
        ${job.oppijaOid},
        ${job.opiskeluoikeusOid},
        ${job.state},
        ${java.sql.Timestamp.valueOf(job.createdAt)},
        ${job.worker},
        ${job.attempts}
      )
      RETURNING *
      """.as[KielitutkintotodistusTiedoteJob]).head
  }

  def setCompleted(id: String): Boolean =
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = ${KielitutkintotodistusTiedoteState.COMPLETED},
          completed_at = now()
      WHERE id = ${id}::uuid
        AND state = ${KielitutkintotodistusTiedoteState.SENDING}
      """.asUpdate) != 0

  def setFailed(id: String, error: String): Boolean =
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = ${KielitutkintotodistusTiedoteState.ERROR},
          error = $error,
          completed_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def findNextRetryable(maxAttempts: Int): Option[KielitutkintotodistusTiedoteJob] = {
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = ${KielitutkintotodistusTiedoteState.SENDING},
          worker = $workerId,
          attempts = attempts + 1,
          error = NULL,
          completed_at = NULL
      WHERE id IN (
        SELECT id
        FROM kielitutkintotodistus_tiedote_job
        WHERE state = ${KielitutkintotodistusTiedoteState.ERROR}
          AND attempts < $maxAttempts
        ORDER BY completed_at
        LIMIT 1
      )
      RETURNING *
      """.as[KielitutkintotodistusTiedoteJob]).headOption
  }

  def countByState: Map[String, Int] = {
    runDbSync(sql"""
      SELECT state, count(*)::int
      FROM kielitutkintotodistus_tiedote_job
      GROUP BY state
      """.as[(String, Int)]
    ).toMap
  }

  def findAll(limit: Int, offset: Int, state: Option[String] = None): Seq[KielitutkintotodistusTiedoteJob] = {
    state match {
      case Some(s) =>
        runDbSync(sql"""
          SELECT *
          FROM kielitutkintotodistus_tiedote_job
          WHERE state = $s
          ORDER BY created_at DESC
          LIMIT $limit OFFSET $offset
          """.as[KielitutkintotodistusTiedoteJob])
      case None =>
        runDbSync(sql"""
          SELECT *
          FROM kielitutkintotodistus_tiedote_job
          ORDER BY created_at DESC
          LIMIT $limit OFFSET $offset
          """.as[KielitutkintotodistusTiedoteJob])
    }
  }

  def truncateForLocal(): Int = {
    require(
      Environment.isUnitTestEnvironment(config) || Environment.isLocalDevelopmentEnvironment(config),
      "truncateForLocal can only be used in local test environment"
    )
    runDbSync(sql"TRUNCATE TABLE kielitutkintotodistus_tiedote_job".asUpdate)
  }

  implicit private val getJobResult: GetResult[KielitutkintotodistusTiedoteJob] = GetResult[KielitutkintotodistusTiedoteJob](r => {
    KielitutkintotodistusTiedoteJob(
      id = r.rs.getString("id"),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      state = r.rs.getString("state"),
      createdAt = r.rs.getTimestamp("created_at").toLocalDateTime,
      completedAt = Option(r.rs.getTimestamp("completed_at")).map(_.toLocalDateTime),
      worker = Option(r.rs.getString("worker")),
      attempts = r.rs.getInt("attempts"),
      error = Option(r.rs.getString("error"))
    )
  })
}
