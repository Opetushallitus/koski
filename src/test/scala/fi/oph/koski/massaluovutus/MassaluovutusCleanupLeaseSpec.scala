package fi.oph.koski.massaluovutus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.TestEnvironment
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{Duration, LocalDateTime}
import java.util.UUID

class MassaluovutusCleanupLeaseSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterEach {
  private val app = KoskiApplicationForTests
  private val db = app.masterDatabase.db

  override protected def beforeEach(): Unit = {
    QueryMethods.runDbSync(db, sql"TRUNCATE TABLE massaluovutus".asUpdate)
    QueryMethods.runDbSync(db, sql"DELETE FROM worker_lease".asUpdate)
    super.beforeEach()
  }

  "cleanup requeues orphaned running queries based on active leases" in {
    val activeHolder = "active-worker"
    app.workerLeaseRepository.tryAcquireOrRenew("massaluovutus", 1, activeHolder, Duration.ofSeconds(30)) should be(true)

    val queryId = UUID.randomUUID().toString
    val query = MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki))
    val session = StorableSession(MockUsers.helsinkiKatselija).toJson
    val running = RunningQuery(
      queryId = queryId,
      userOid = MockUsers.helsinkiKatselija.oid,
      query = query,
      createdAt = LocalDateTime.now(),
      startedAt = LocalDateTime.now(),
      worker = "orphan-worker",
      resultFiles = Nil,
      session = session,
      meta = None,
      progress = None
    )
    app.massaluovutusService.addRaw(running)

    app.massaluovutusCleanupScheduler.trigger()

    val state = QueryMethods
      .runDbSync(db, sql"SELECT state FROM massaluovutus WHERE id = $queryId::uuid".as[String])
      .head
    state should equal(QueryState.pending)
  }

  "cleanup requeues when lease has expired" in {
    val expiringHolder = "expiring-worker"
    app.workerLeaseRepository.tryAcquireOrRenew("massaluovutus", 1, expiringHolder, Duration.ofMillis(100)) should be(true)

    val queryId = UUID.randomUUID().toString
    val query = MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki))
    val session = StorableSession(MockUsers.helsinkiKatselija).toJson
    val running = RunningQuery(
      queryId = queryId,
      userOid = MockUsers.helsinkiKatselija.oid,
      query = query,
      createdAt = LocalDateTime.now(),
      startedAt = LocalDateTime.now(),
      worker = expiringHolder,
      resultFiles = Nil,
      session = session,
      meta = None,
      progress = None
    )
    app.massaluovutusService.addRaw(running)

    Thread.sleep(150)
    app.massaluovutusCleanupScheduler.trigger()

    val state = QueryMethods
      .runDbSync(db, sql"SELECT state FROM massaluovutus WHERE id = $queryId::uuid".as[String])
      .head
    state should equal(QueryState.pending)
  }

  "cleanup does not requeue when lease is active for worker" in {
    val activeHolder = "active-worker"
    app.workerLeaseRepository.tryAcquireOrRenew("massaluovutus", 1, activeHolder, Duration.ofSeconds(30)) should be(true)

    val queryId = UUID.randomUUID().toString
    val query = MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki))
    val session = StorableSession(MockUsers.helsinkiKatselija).toJson
    val running = RunningQuery(
      queryId = queryId,
      userOid = MockUsers.helsinkiKatselija.oid,
      query = query,
      createdAt = LocalDateTime.now(),
      startedAt = LocalDateTime.now(),
      worker = activeHolder,
      resultFiles = Nil,
      session = session,
      meta = None,
      progress = None
    )
    app.massaluovutusService.addRaw(running)

    app.massaluovutusCleanupScheduler.trigger()

    val state = QueryMethods
      .runDbSync(db, sql"SELECT state FROM massaluovutus WHERE id = $queryId::uuid".as[String])
      .head
    state should equal(QueryState.running)
  }
}
