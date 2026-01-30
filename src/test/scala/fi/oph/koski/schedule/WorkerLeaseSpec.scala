package fi.oph.koski.schedule

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.util.Wait
import fi.oph.koski.TestEnvironment
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class WorkerLeaseSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterEach {
  private val db = KoskiApplicationForTests.masterDatabase.db
  private val repo = new WorkerLeaseRepository(db)

  override protected def beforeEach(): Unit = {
    QueryMethods.runDbSync(db, sql"DELETE FROM worker_lease".asUpdate)
    super.beforeEach()
  }

  "WorkerLeaseRepository" - {
    "acquires and renews lease for same holder" in {
      repo.tryAcquireOrRenew("test", 1, "holder-a", Duration.ofSeconds(1)) should be(true)
      repo.tryAcquireOrRenew("test", 1, "holder-b", Duration.ofSeconds(1)) should be(false)
      repo.tryAcquireOrRenew("test", 1, "holder-a", Duration.ofSeconds(1)) should be(true)
      repo.activeHolders("test") should contain("holder-a")
    }

    "allows takeover after expiry" in {
      repo.tryAcquireOrRenew("test", 1, "holder-a", Duration.ofMillis(100)) should be(true)
      Thread.sleep(150)
      repo.tryAcquireOrRenew("test", 1, "holder-b", Duration.ofSeconds(1)) should be(true)
    }
  }

  "WorkerLeaseElector" - {
    "acquires and releases lease" in {
      val elector = new WorkerLeaseElector(
        repo,
        name = "test",
        holderId = "holder-a",
        slots = 1,
        leaseDuration = Duration.ofMillis(300),
        heartbeatInterval = Duration.ofMillis(50)
      )

      elector.start()
      Wait.until(elector.hasLease, timeoutMs = 1000)
      repo.activeHolders("test") should contain("holder-a")

      elector.shutdown()
      Wait.until(repo.activeHolders("test").isEmpty, timeoutMs = 1000)
    }

    "enforces max concurrent slots" in {
      val electorA = new WorkerLeaseElector(
        repo,
        name = "multi",
        holderId = "holder-a",
        slots = 2,
        leaseDuration = Duration.ofMillis(300),
        heartbeatInterval = Duration.ofMillis(50)
      )
      val electorB = new WorkerLeaseElector(
        repo,
        name = "multi",
        holderId = "holder-b",
        slots = 2,
        leaseDuration = Duration.ofMillis(300),
        heartbeatInterval = Duration.ofMillis(50)
      )
      val electorC = new WorkerLeaseElector(
        repo,
        name = "multi",
        holderId = "holder-c",
        slots = 2,
        leaseDuration = Duration.ofMillis(300),
        heartbeatInterval = Duration.ofMillis(50)
      )

      electorA.start()
      electorB.start()
      Wait.until(repo.activeHolders("multi").size == 2, timeoutMs = 1000)

      electorC.start()
      Thread.sleep(200)
      val holders = repo.activeHolders("multi")
      holders.size should be(2)
      holders should not contain "holder-c"

      electorA.shutdown()
      electorB.shutdown()
      electorC.shutdown()
    }

    "loses lease to faster elector and reacquires after release" in {
      val lost = new AtomicBoolean(false)
      val electorA = new WorkerLeaseElector(
        repo,
        name = "reacquire",
        holderId = "holder-a",
        slots = 1,
        leaseDuration = Duration.ofMillis(100),
        heartbeatInterval = Duration.ofMillis(200)
      )
      val electorB = new WorkerLeaseElector(
        repo,
        name = "reacquire",
        holderId = "holder-b",
        slots = 1,
        leaseDuration = Duration.ofMillis(100),
        heartbeatInterval = Duration.ofMillis(50)
      )

      electorA.start(onLost = _ => lost.set(true))
      Wait.until(electorA.hasLease, timeoutMs = 1000)

      electorB.start()
      Wait.until(repo.activeHolders("reacquire").contains("holder-b"), timeoutMs = 1000)
      Wait.until(!electorA.hasLease && lost.get, timeoutMs = 1000)

      electorB.shutdown()
      Wait.until(electorA.hasLease, timeoutMs = 1000)

      electorA.shutdown()
    }
  }
}
