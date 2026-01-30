package fi.oph.koski.schedule

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log.Logging

import java.time.Duration

class WorkerLeaseRepository(val db: DB) extends QueryMethods with Logging {
  def tryAcquireOrRenew(name: String, slot: Int, holderId: String, leaseDuration: Duration): Boolean = {
    val millis = leaseDuration.toMillis

    runDbSync(
      sql"""
      INSERT INTO worker_lease(name, slot, holder_id, expires_at, heartbeat_at)
      VALUES ($name, $slot, $holderId, now() + $millis * interval '1 millisecond', now())
      ON CONFLICT (name, slot) DO UPDATE
        SET holder_id = EXCLUDED.holder_id,
            expires_at = EXCLUDED.expires_at,
            heartbeat_at = EXCLUDED.heartbeat_at
      WHERE worker_lease.expires_at < now()
         OR worker_lease.holder_id = EXCLUDED.holder_id
      RETURNING holder_id
      """.as[String]
    ).headOption.contains(holderId)
  }

  def release(name: String, slot: Int, holderId: String): Unit = {
    runDbSync(
      sql"""
      DELETE FROM worker_lease
      WHERE name = $name
        AND slot = $slot
        AND holder_id = $holderId
      """.asUpdate
    )
  }

  def activeHolders(name: String): Seq[String] =
    runDbSync(
      sql"""
      SELECT DISTINCT holder_id
      FROM worker_lease
      WHERE name = $name
        AND expires_at > now()
      """.as[String]
    )
}
