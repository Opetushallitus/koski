package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.TestEnvironment
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{Duration, LocalDateTime}
import java.util.UUID

class TodistusCleanupLeaseSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterEach {
  private val app = KoskiApplicationForTests
  private val db = app.masterDatabase.db

  override protected def beforeEach(): Unit = {
    QueryMethods.runDbSync(db, sql"TRUNCATE TABLE todistus_job".asUpdate)
    QueryMethods.runDbSync(db, sql"DELETE FROM worker_lease WHERE name = 'todistus'".asUpdate)
    super.beforeEach()
  }

  "cleanup requeues orphaned todistus jobs based on active leases" in {
    val activeHolder = "active-worker"
    app.workerLeaseRepository.tryAcquireOrRenew("todistus", 1, activeHolder, Duration.ofSeconds(30)) should be(true)

    val id = UUID.randomUUID().toString
    val userOid = "1.2.246.562.24.00000000001"
    val oppijaOid = "1.2.246.562.24.00000000002"
    val opiskeluoikeusOid = "1.2.246.562.15.00000000003"
    val orphanWorker = "orphan-worker"
    val now = LocalDateTime.now()

    QueryMethods.runDbSync(db, sql"""
      INSERT INTO todistus_job(
        id, user_oid, oppija_oid, opiskeluoikeus_oid, language, state, created_at, started_at, worker, attempts
      ) VALUES (
        $id::uuid, $userOid, $oppijaOid, $opiskeluoikeusOid, ${TodistusLanguage.FI}, ${TodistusState.GATHERING_INPUT},
        ${java.sql.Timestamp.valueOf(now)}, ${java.sql.Timestamp.valueOf(now)}, $orphanWorker, 0
      )
      """.asUpdate)

    val activeWorkers = app.workerLeaseRepository.activeHolders("todistus")
    app.todistusService.cleanup(activeWorkers)

    val result = QueryMethods.runDbSync(db, sql"""
      SELECT state, worker
      FROM todistus_job
      WHERE id = $id::uuid
      """.as[(String, Option[String])]).head

    result._1 should equal(TodistusState.QUEUED)
    result._2 should be(None)
  }

  "cleanup does not requeue QUEUED or COMPLETED jobs from orphan workers" in {
    val orphanWorker = "orphan-worker"
    val userOid = "1.2.246.562.24.00000000001"
    val oppijaOid = "1.2.246.562.24.00000000002"
    val now = LocalDateTime.now()

    val queuedId = UUID.randomUUID().toString
    QueryMethods.runDbSync(db, sql"""
      INSERT INTO todistus_job(
        id, user_oid, oppija_oid, opiskeluoikeus_oid, language, state, created_at, worker, attempts
      ) VALUES (
        $queuedId::uuid, $userOid, $oppijaOid, ${"1.2.246.562.15.00000000010"}, ${TodistusLanguage.FI}, ${TodistusState.QUEUED},
        ${java.sql.Timestamp.valueOf(now)}, $orphanWorker, 1
      )
      """.asUpdate)

    val completedId = UUID.randomUUID().toString
    QueryMethods.runDbSync(db, sql"""
      INSERT INTO todistus_job(
        id, user_oid, oppija_oid, opiskeluoikeus_oid, language, state, created_at, started_at, completed_at, worker, attempts
      ) VALUES (
        $completedId::uuid, $userOid, $oppijaOid, ${"1.2.246.562.15.00000000011"}, ${TodistusLanguage.FI}, ${TodistusState.COMPLETED},
        ${java.sql.Timestamp.valueOf(now)}, ${java.sql.Timestamp.valueOf(now)}, ${java.sql.Timestamp.valueOf(now)}, $orphanWorker, 1
      )
      """.asUpdate)

    app.todistusService.cleanup(Seq.empty)

    val queuedResult = QueryMethods.runDbSync(db, sql"""
      SELECT state, worker FROM todistus_job WHERE id = $queuedId::uuid
      """.as[(String, Option[String])]).head
    queuedResult._1 should equal(TodistusState.QUEUED)
    queuedResult._2 should be(Some(orphanWorker))

    val completedResult = QueryMethods.runDbSync(db, sql"""
      SELECT state, worker FROM todistus_job WHERE id = $completedId::uuid
      """.as[(String, Option[String])]).head
    completedResult._1 should equal(TodistusState.COMPLETED)
    completedResult._2 should be(Some(orphanWorker))
  }

  "cleanup does not requeue when lease is active for worker" in {
    val activeHolder = "active-worker"
    app.workerLeaseRepository.tryAcquireOrRenew("todistus", 1, activeHolder, Duration.ofSeconds(30)) should be(true)

    val id = UUID.randomUUID().toString
    val userOid = "1.2.246.562.24.00000000001"
    val oppijaOid = "1.2.246.562.24.00000000002"
    val opiskeluoikeusOid = "1.2.246.562.15.00000000003"
    val now = LocalDateTime.now()

    QueryMethods.runDbSync(db, sql"""
      INSERT INTO todistus_job(
        id, user_oid, oppija_oid, opiskeluoikeus_oid, language, state, created_at, started_at, worker, attempts
      ) VALUES (
        $id::uuid, $userOid, $oppijaOid, $opiskeluoikeusOid, ${TodistusLanguage.FI}, ${TodistusState.GATHERING_INPUT},
        ${java.sql.Timestamp.valueOf(now)}, ${java.sql.Timestamp.valueOf(now)}, $activeHolder, 0
      )
      """.asUpdate)

    val activeWorkers = app.workerLeaseRepository.activeHolders("todistus")
    app.todistusService.cleanup(activeWorkers)

    val result = QueryMethods.runDbSync(db, sql"""
      SELECT state, worker
      FROM todistus_job
      WHERE id = $id::uuid
      """.as[(String, Option[String])]).head

    result._1 should equal(TodistusState.GATHERING_INPUT)
    result._2 should be(Some(activeHolder))
  }
}
