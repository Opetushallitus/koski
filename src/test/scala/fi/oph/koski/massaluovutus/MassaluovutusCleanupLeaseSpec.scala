package fi.oph.koski.massaluovutus

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

/**
 * Testaa MassaluovutusService.cleanupin orpojen kyselyiden käsittelyä. Aktiiviset workerit
 * annetaan cleanupille suoraan parametrina — testi ei saa varata jaettua 'massaluovutus'-
 * worker_leasea eikä poistaa sen rivejä, koska sovelluksen oma kyselyscheduler käyttää samoja
 * slotteja. Varaus pysäyttäisi kyselyiden ajon lease-varauksen ajaksi, ja rivien poisto jättäisi
 * sovelluksen hetkeksi tilaan, jossa se luulee omistavansa leasen mutta ei näy aktiivisena
 * workerina — jolloin cleanup vapauttaa sen ajossa olevat kyselyt kesken kaiken.
 */
class MassaluovutusCleanupLeaseSpec extends AnyFreeSpec with MassaluovutusTestMethods with TestEnvironment with Matchers with BeforeAndAfterEach {
  private val db = app.masterDatabase.db

  // Vain tämän testin käytössä oleva lease-nimi, jolla ei ole schedulereita.
  private val testLeaseName = "massaluovutus-cleanup-lease-spec"

  override protected def beforeEach(): Unit = {
    QueryMethods.runDbSync(db, sql"TRUNCATE TABLE massaluovutus".asUpdate)
    QueryMethods.runDbSync(db, sql"DELETE FROM worker_lease WHERE name = $testLeaseName".asUpdate)
    super.beforeEach()
  }

  private def createRunningQuery(worker: String): RunningQuery =
    RunningQuery(
      queryId = UUID.randomUUID().toString,
      userOid = MockUsers.helsinkiKatselija.oid,
      query = MassaluovutusQueryLuokalleJaaneetJson(organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki)),
      createdAt = LocalDateTime.now(),
      startedAt = LocalDateTime.now(),
      worker = worker,
      resultFiles = Nil,
      session = StorableSession(MockUsers.helsinkiKatselija).toJson,
      meta = None,
      progress = None
    )

  private def stateOf(queryId: String): String =
    QueryMethods
      .runDbSync(db, sql"SELECT state FROM massaluovutus WHERE id = $queryId::uuid".as[String])
      .head

  "cleanup requeues orphaned running queries based on active leases" in {
    withoutRunningQueryScheduler {
      val running = createRunningQuery("orphan-worker")
      app.massaluovutusService.addRaw(running)

      app.massaluovutusService.cleanup(Seq("active-worker"))

      stateOf(running.queryId) should equal(QueryState.pending)
    }
  }

  "cleanup requeues when lease has expired" in {
    withoutRunningQueryScheduler {
      val expiringHolder = "expiring-worker"
      app.workerLeaseRepository.tryAcquireOrRenew(testLeaseName, 1, expiringHolder, Duration.ofMillis(100)) should be(true)

      val running = createRunningQuery(expiringHolder)
      app.massaluovutusService.addRaw(running)

      Thread.sleep(150)
      val activeWorkers = app.workerLeaseRepository.activeHolders(testLeaseName)
      activeWorkers should not contain expiringHolder

      app.massaluovutusService.cleanup(activeWorkers)

      stateOf(running.queryId) should equal(QueryState.pending)
    }
  }

  "cleanup does not requeue when lease is active for worker" in {
    withoutRunningQueryScheduler {
      val activeHolder = "active-worker"
      app.workerLeaseRepository.tryAcquireOrRenew(testLeaseName, 1, activeHolder, Duration.ofSeconds(30)) should be(true)

      val running = createRunningQuery(activeHolder)
      app.massaluovutusService.addRaw(running)

      val activeWorkers = app.workerLeaseRepository.activeHolders(testLeaseName)
      activeWorkers should contain(activeHolder)

      app.massaluovutusService.cleanup(activeWorkers)

      stateOf(running.queryId) should equal(QueryState.running)
    }
  }
}
