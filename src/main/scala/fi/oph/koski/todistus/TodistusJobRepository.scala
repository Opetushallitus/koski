package fi.oph.koski.todistus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

import java.time.LocalDateTime

class TodistusJobRepository(val db: DB, val workerId: String, config: Config) extends QueryMethods with Logging with DatabaseConverters {

  def get(id: String): Either[HttpStatus, TodistusJob] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus_job
      WHERE id = ${id}::uuid
      """.as[TodistusJob]
    ).headOption
      .toRight(KoskiErrorCategory.notFound())
  }

  def get(id: String, kelpuutetutOppijaOidit: Set[String]): Either[HttpStatus, TodistusJob] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus_job
      WHERE id = ${id}::uuid
        AND oppija_oid = ANY(${kelpuutetutOppijaOidit.toList})
      """.as[TodistusJob]
    ).headOption
      .toRight(KoskiErrorCategory.notFound())
  }

  def findByParameters(
    opiskeluoikeusOid: String,
    language: String,
    opiskeluoikeusVersionumero: Int,
    oppijaHenkilötiedotHash: String
  ): Option[TodistusJob] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus_job
      WHERE opiskeluoikeus_oid = ${opiskeluoikeusOid}
        AND language = ${language}
        AND NOT state = ANY(${TodistusState.nonReusableStates.toSeq})
        AND (
          -- QUEUED-tilassa oleva todistus matchaa aina (ei vielä hash/versionumero)
          state = ${TodistusState.QUEUED}
          OR
          -- Muissa tiloissa hash ja versionumero täytyy täsmätä
          (opiskeluoikeus_versionumero = ${opiskeluoikeusVersionumero}
           AND oppija_henkilotiedot_hash = ${oppijaHenkilötiedotHash})
        )
      ORDER BY created_at DESC
      LIMIT 1
      """.as[TodistusJob]
    ).headOption
  }

  def addOrReuseExisting(todistusJob: TodistusJob)(implicit user: KoskiSpecificSession): Either[HttpStatus, TodistusJob] = {
    if (todistusJob.userOid.contains(user.oid) || user.hasRole(OPHPAAKAYTTAJA)) {
      // Huom! Tämä CTE ei ole täysin robusti: jos tehdään 2 todistuksen luontia samaan aikaan, on mahdollista, että syntyy 2 jobia,
      // jos kummatkin tekevät ensin SELECT:in ennenkuin kumpikaan tekee INSERT-osuutta. Tämän korjaaminen on kuitenkin vaikeaa täysin robustisti,
      // koska henkilötiedot hash ja versionumero lukitaan vasta todistus-jobia suoritettaessa. Jos esim. tekisi unique constraintin opiskeluoikeus_oidille ja languagelle, niin
      // voisi käydä niin, että todistus luodaan vanhemmasta oo-versiosta kuin on ollut saatavilla sillä hetkellä kun toinen jonoonlisäysyritys on tehty.
      // Jos silloin tällöin syntyy harvinaisessa tilanteessa 2 jobia, ei se ole oikea ongelma.
      runDbSync(
        sql"""
        WITH existing AS (
          SELECT *
          FROM todistus_job
          WHERE oppija_oid = ${todistusJob.oppijaOid}
            AND opiskeluoikeus_oid = ${todistusJob.opiskeluoikeusOid}
            AND language = ${todistusJob.language}
            AND NOT state = ANY(${TodistusState.nonReusableStates.toSeq})
            AND (
              -- QUEUED-tilassa oleva job matchaa aina (ei vielä hash/versionumero)
              state = ${TodistusState.QUEUED}
              OR
              -- Muissa tiloissa hash ja versionumero täytyy täsmätä
              (opiskeluoikeus_versionumero = ${todistusJob.opiskeluoikeusVersionumero}
               AND oppija_henkilotiedot_hash = ${todistusJob.oppijaHenkilötiedotHash}
               AND oppija_henkilotiedot_hash IS NOT NULL
               AND opiskeluoikeus_versionumero IS NOT NULL)
            )
          ORDER BY created_at DESC
          LIMIT 1
          FOR UPDATE
        ),
        inserted AS (
          -- Huomaa, että versionumeroa ja hash:iä ei tallenneta: ne tallennetaan vasta luontihetkellä, jotta saadaan talteen
          -- täsmälliset arvot, jotka esiintyvät myös generoidussa PDF-todistuksessa.
          INSERT INTO todistus_job(id, user_oid, oppija_oid, opiskeluoikeus_oid, language,
                              state,
                              created_at, started_at, completed_at, worker, attempts, error)
          SELECT
            ${todistusJob.id}::uuid,
            ${todistusJob.userOid},
            ${todistusJob.oppijaOid},
            ${todistusJob.opiskeluoikeusOid},
            ${todistusJob.language},
            ${todistusJob.state},
            ${java.sql.Timestamp.valueOf(todistusJob.createdAt)},
            ${todistusJob.startedAt.map(java.sql.Timestamp.valueOf)},
            ${todistusJob.completedAt.map(java.sql.Timestamp.valueOf)},
            ${todistusJob.worker},
            ${todistusJob.attempts},
            ${todistusJob.error}
          WHERE NOT EXISTS (SELECT 1 FROM existing)
          RETURNING *
        )
        SELECT * FROM existing
        UNION ALL
        SELECT * FROM inserted
        """.as[TodistusJob]
      ).headOption
        .toRight(KoskiErrorCategory.notFound())
    } else {
      val msg = s"Käyttäjä ${user.oid} yritti lisätä todistuksen oppijalle ${todistusJob.oppijaOid} (käyttäjä: ${todistusJob.userOid.getOrElse("unknown")}) ilman oikeuksia"
      logger.error(msg)
      Left(KoskiErrorCategory.forbidden())
    }
  }

  def updateState(id: String, startState: String, state: String, completedAt: Option[LocalDateTime] = None): Either[HttpStatus, TodistusJob] = {
    runDbSync(sql"""
      UPDATE todistus_job
      SET
        state = $state,
        completed_at = ${completedAt.map(java.sql.Timestamp.valueOf)}
      WHERE id = ${id}::uuid AND
        state = $startState
      RETURNING *
      """.as[TodistusJob]).headOption
      .toRight({
        val error = s"Failed to update state ${startState} => ${state} for id ${id}"
        logger.error(error)
        KoskiErrorCategory.notFound(error)
      })
  }

  def updateStateWithHashAndVersion(id: String, startState: String, state: String, hash: String, versionumero: Int): Either[HttpStatus, TodistusJob] = {
    runDbSync(sql"""
      UPDATE todistus_job
      SET
        state = $state,
        oppija_henkilotiedot_hash = $hash,
        opiskeluoikeus_versionumero = $versionumero
      WHERE id = ${id}::uuid AND
        state = $startState
      RETURNING *
      """.as[TodistusJob]).headOption
      .toRight({
        val error = s"Failed to update state ${startState} => ${state} for id ${id}"
        logger.error(error)
        KoskiErrorCategory.notFound(error)
      })
  }

  def takeNext: Option[TodistusJob] = {
    runDbSync(sql"""
      UPDATE todistus_job
      SET
        state = 'GATHERING_INPUT',
        worker = $workerId,
        started_at = now(),
        attempts = attempts + 1
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

  def requeueJob(id: String): Boolean =
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET
        state = ${TodistusState.QUEUED},
        worker = NULL
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def setJobFailed(id: String, error: String): Boolean =
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET
        state = ${TodistusState.ERROR},
        error = $error,
        completed_at = now()
      WHERE id = ${id}::uuid
      """.asUpdate) != 0

  def markAllMyJobsInterrupted(): Boolean =
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET
        state = ${TodistusState.INTERRUPTED}
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

  def numberOfRunningJobs: Int =
    runDbSync(sql"""
      SELECT count(*)
      FROM todistus_job
      WHERE state = any(${TodistusState.runningStates.toSeq})
      """.as[Int]).head

  def findOrphanedJobs(koskiInstances: Seq[String]): Seq[TodistusJob] =
    runDbSync(
      sql"""
      SELECT *
      FROM todistus_job
      WHERE NOT state = any(${TodistusState.nonReusableStates.toSeq})
        AND NOT worker = any($koskiInstances)
      """.as[TodistusJob])

  def truncateForLocal(): Int = {
    require(Environment.isUnitTestEnvironment(config) || Environment.isLocalDevelopmentEnvironment(config), "truncateForLocal can only be used in local test environment")

    runDbSync(sql"TRUNCATE TABLE todistus_job".asUpdate)
  }

  def setJobQueuedForExpireForUnitTests(id: String): Boolean = {
    require(Environment.isUnitTestEnvironment(config), "setJobQueuedForExpireForUnitTests can only be used in unit test environment")
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET state = ${TodistusState.QUEUED_FOR_EXPIRE}
      WHERE id = ${id}::uuid
      """.asUpdate) != 0
  }

  def setJobExpiredForUnitTests(id: String): Boolean = {
    require(Environment.isUnitTestEnvironment(config), "setJobExpiredForUnitTests can only be used in unit test environment")
    runDbSync(
      sql"""
      UPDATE todistus_job
      SET state = ${TodistusState.EXPIRED}
      WHERE id = ${id}::uuid
      """.asUpdate) != 0
  }

  def addRawForUnitTests(todistusJob: TodistusJob): TodistusJob = {
    require(Environment.isUnitTestEnvironment(config), "addRawForUnitTests can only be used in unit test environment")
    runDbSync(sql"""
      INSERT INTO todistus_job(id, user_oid, oppija_oid, opiskeluoikeus_oid, language,
                          opiskeluoikeus_versionumero, oppija_henkilotiedot_hash, state,
                          created_at, started_at, completed_at, worker, attempts, error)
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
        ${todistusJob.attempts},
        ${todistusJob.error}
      )
      RETURNING *
      """.as[TodistusJob]).head
  }

  def getFromDbForUnitTests(id: String): Option[TodistusJob] = {
    require(Environment.isUnitTestEnvironment(config), "getFromDbForUnitTests can only be used in unit test environment")
    runDbSync(sql"""
      SELECT *
      FROM todistus_job
      WHERE id = ${id}::uuid
      """.as[TodistusJob]
    ).headOption
  }

  implicit private val getTodistusJobResult: GetResult[TodistusJob] = GetResult[TodistusJob](r => {
    TodistusJob(
      id = r.rs.getString("id"),
      userOid = Option(r.rs.getString("user_oid")),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      language = r.rs.getString("language"),
      opiskeluoikeusVersionumero = {
        val value = r.rs.getInt("opiskeluoikeus_versionumero")
        if (r.rs.wasNull()) None else Some(value)
      },
      oppijaHenkilötiedotHash = Option(r.rs.getString("oppija_henkilotiedot_hash")),
      state = r.rs.getString("state"),
      createdAt = r.rs.getTimestamp("created_at").toLocalDateTime,
      startedAt = Option(r.rs.getTimestamp("started_at")).map(_.toLocalDateTime),
      completedAt = Option(r.rs.getTimestamp("completed_at")).map(_.toLocalDateTime),
      worker = Option(r.rs.getString("worker")),
      attempts = {
        val value = r.rs.getInt("attempts")
        if (r.rs.wasNull()) None else Some(value)
      },
      error = Option(r.rs.getString("error"))
    )
  })
}
