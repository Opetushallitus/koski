package fi.oph.koski.todistus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, DatabaseConverters, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import slick.jdbc.GetResult

import java.time.LocalDateTime
import java.util.UUID

class TodistusRepository(val db: DB, workerId: String) extends QueryMethods with Logging with DatabaseConverters {

  def get(id: UUID)(implicit user: KoskiSpecificSession): Option[Todistus] = {
    runDbSync(sql"""
      SELECT *
      FROM todistus
      WHERE id = ${id.toString}::uuid
        AND (user_oid = ${user.oid} OR ${user.hasGlobalReadAccess})
      """.as[Todistus]
    ).headOption
  }

  def add(todistus: Todistus): Todistus = {
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
      """.as[Todistus]).head
  }

  def updateState(id: UUID, state: String): Boolean = {
    runDbSync(sql"""
      UPDATE todistus
      SET state = $state
      WHERE id = ${id.toString}::uuid
      """.asUpdate) != 0
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

  implicit private val getTodistusResult: GetResult[Todistus] = GetResult[Todistus](r => {
    Todistus(
      id = UUID.fromString(r.rs.getString("id")),
      userOid = r.rs.getString("user_oid"),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid"),
      language = r.rs.getString("language"),
      opiskeluoikeusVersionumero = r.rs.getInt("opiskeluoikeus_versionumero"),
      oppijaHenkilotiedotHash = r.rs.getString("oppija_henkilotiedot_hash"),
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

case class Todistus(
  id: UUID,
  userOid: String,
  oppijaOid: String,
  opiskeluoikeusOid: String,
  language: String,
  opiskeluoikeusVersionumero: Int,
  oppijaHenkilotiedotHash: String,
  state: String,
  createdAt: LocalDateTime,
  startedAt: Option[LocalDateTime] = None,
  completedAt: Option[LocalDateTime] = None,
  worker: Option[String] = None,
  rawS3ObjectKey: Option[String] = None,
  signedS3ObjectKey: Option[String] = None,
  error: Option[String] = None
)
