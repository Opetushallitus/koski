package fi.oph.koski.todistus.tiedote

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

import java.time.LocalDateTime

class KielitutkintotodistusTiedoteRepository(val db: DB, val workerId: String, config: Config) extends QueryMethods with Logging with DatabaseConverters {

  private val earliestDate = config.getString("tiedote.earliestDate")
  private val gracePeriodHours = config.getInt("tiedote.gracePeriodHours")

  def findEligibleBatch(limit: Int): Seq[(String, String, Int)] = {
    runDbSync(sql"""
      SELECT oo.oid, oo.oppija_oid, oo.versionumero
      FROM opiskeluoikeus oo
      JOIN opiskeluoikeushistoria h ON h.opiskeluoikeus_id = oo.id AND h.versionumero = 1
      WHERE oo.koulutusmuoto = 'kielitutkinto'
        AND 'yleinenkielitutkinto' = ANY(oo.suoritustyypit)
        AND NOT oo.mitatoity
        AND NOT oo.poistettu
        AND oo.data #>> '{suoritukset,0,vahvistus}' IS NOT NULL
        AND (oo.data #>> '{suoritukset,0,vahvistus,päivä}')::date >= ${earliestDate}::date
        AND h.aikaleima <= NOW() - ($gracePeriodHours * INTERVAL '1 hour')
        AND NOT EXISTS (
          SELECT 1 FROM kielitutkintotodistus_tiedote_job tj
          WHERE tj.opiskeluoikeus_oid = oo.oid
        )
      ORDER BY oo.aikaleima
      LIMIT $limit
      """.as[(String, String, Int)])
  }

  def add(job: KielitutkintotodistusTiedoteJob): KielitutkintotodistusTiedoteJob = {
    runDbSync(sql"""
      INSERT INTO kielitutkintotodistus_tiedote_job(id, oppija_oid, opiskeluoikeus_oid, state, created_at, completed_at, worker, attempts, error, opiskeluoikeus_versio)
      VALUES (
        ${job.id}::uuid,
        ${job.oppijaOid},
        ${job.opiskeluoikeusOid},
        ${job.state},
        ${java.sql.Timestamp.valueOf(job.createdAt)},
        ${job.completedAt.map(java.sql.Timestamp.valueOf)},
        ${job.worker},
        ${job.attempts},
        ${job.error},
        ${job.opiskeluoikeusVersio}
      )
      RETURNING *
      """.as[KielitutkintotodistusTiedoteJob]).head
  }

  def setCompleted(id: String, opiskeluoikeusVersio: Int): Boolean =
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = ${KielitutkintotodistusTiedoteState.COMPLETED},
          error = NULL,
          completed_at = now(),
          attempts = attempts + 1,
          opiskeluoikeus_versio = $opiskeluoikeusVersio
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setFailed(id: String, error: String): Boolean =
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = ${KielitutkintotodistusTiedoteState.ERROR},
          error = $error,
          attempts = attempts + 1
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def findAllRetryable(maxAttempts: Int, stuckThresholdMinutes: Int): Seq[KielitutkintotodistusTiedoteJob] = {
    runDbSync(sql"""
      SELECT *
      FROM kielitutkintotodistus_tiedote_job
      WHERE attempts < $maxAttempts
        AND (
          state = ${KielitutkintotodistusTiedoteState.ERROR}
          OR (state = ${KielitutkintotodistusTiedoteState.WAITING_FOR_TODISTUS}
              AND created_at < NOW() - ($stuckThresholdMinutes * INTERVAL '1 minute'))
        )
      ORDER BY created_at
      """.as[KielitutkintotodistusTiedoteJob])
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

  def setState(id: String, state: String): Boolean =
    runDbSync(sql"""
      UPDATE kielitutkintotodistus_tiedote_job
      SET state = $state
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def findByState(state: String, limit: Int): Seq[KielitutkintotodistusTiedoteJob] =
    runDbSync(sql"""
      SELECT *
      FROM kielitutkintotodistus_tiedote_job
      WHERE state = $state
      ORDER BY created_at
      LIMIT $limit
      """.as[KielitutkintotodistusTiedoteJob])

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
      error = Option(r.rs.getString("error")),
      opiskeluoikeusVersio = r.rs.getInt("opiskeluoikeus_versio")
    )
  })
}
