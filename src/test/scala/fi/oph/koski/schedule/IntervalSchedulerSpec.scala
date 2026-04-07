package fi.oph.koski.schedule

import java.time.Duration
import java.time.Duration.{ofMillis => millis}
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.db.{KoskiTables, QueryMethods}
import fi.oph.koski.util.Wait
import org.json4s.{JInt, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._

class IntervalSchedulerSpec extends AnyFreeSpec with TestEnvironment with Matchers {


  private def resetSchedulerRow(name: String): Unit =
    QueryMethods.runDbSync(KoskiApplicationForTests.masterDatabase.db, sqlu"DELETE FROM scheduler WHERE name = $name")

  "Basic tests where both schedulers work exactly the same" - {
    "fires task according to interval" - {
      "GlobalIntervalScheduler" in {
        val schedulerName = "global-basic"

        resetSchedulerRow(schedulerName)

        val executionCount = new AtomicInteger(0)
        val scheduler = GlobalIntervalScheduler(
          KoskiApplicationForTests, schedulerName, millis(1),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 1
        )
        Wait.until(executionCount.get >= 2, timeoutMs = 5000)
        scheduler.shutdown()
      }

      "IndependentIntervalScheduler" in {
        val executionCount = new AtomicInteger(0)
        val scheduler = IndependentIntervalScheduler(
          KoskiApplicationForTests, "independent-basic", millis(1),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 1, concurrency = 0
        )
        Wait.until(executionCount.get >= 2, timeoutMs = 5000)
        scheduler.shutdown()
      }
    }

    "does not run overlapping tasks" - {
      "GlobalIntervalScheduler" in {
        val schedulerName = "global-no-overlap"

        resetSchedulerRow(schedulerName)

        val sharedResource = new AtomicInteger(0)
        val scheduler = GlobalIntervalScheduler(
          KoskiApplicationForTests, schedulerName, millis(1),
          () => { Thread.sleep(500); sharedResource.incrementAndGet() },
          shouldFireCheckIntervalMillis = 1
        )
        val start = System.currentTimeMillis
        Wait.until(sharedResource.get == 1, timeoutMs = 700)
        (System.currentTimeMillis() - start >= 500) should be(true)
        Wait.until(sharedResource.get == 2, timeoutMs = 500)
        (System.currentTimeMillis() - start >= 1000) should be(true)
        scheduler.shutdown()
      }

      "IndependentIntervalScheduler" in {
        val sharedResource = new AtomicInteger(0)
        val scheduler = IndependentIntervalScheduler(
          KoskiApplicationForTests, "independent-no-overlap", millis(1),
          () => { Thread.sleep(500); sharedResource.incrementAndGet() },
          shouldFireCheckIntervalMillis = 1, concurrency = 0
        )
        val start = System.currentTimeMillis
        Wait.until(sharedResource.get == 1, timeoutMs = 700)
        (System.currentTimeMillis() - start >= 500) should be(true)
        Wait.until(sharedResource.get == 2, timeoutMs = 500)
        (System.currentTimeMillis() - start >= 1000) should be(true)
        scheduler.shutdown()
      }
    }

    "recovers from errors" - {
      "GlobalIntervalScheduler" in {
        val schedulerName = "global-recovery"

        resetSchedulerRow(schedulerName)

        val errorCount = new AtomicInteger(0)
        val successCount = new AtomicInteger(0)
        val scheduler = GlobalIntervalScheduler(
          KoskiApplicationForTests, schedulerName, millis(1),
          () => {
            Thread.sleep(10)
            if (successCount.get() < 1) { errorCount.incrementAndGet(); throw new Exception("error") }
            successCount.incrementAndGet()
          },
          shouldFireCheckIntervalMillis = 1
        )
        Thread.sleep(50)
        errorCount.get should be > 0
        successCount.set(1)
        Wait.until(successCount.get >= 2, timeoutMs = 1000, retryIntervalMs = 10)
        scheduler.shutdown()
      }

      "IndependentIntervalScheduler" in {
        val errorCount = new AtomicInteger(0)
        val successCount = new AtomicInteger(0)
        val scheduler = IndependentIntervalScheduler(
          KoskiApplicationForTests, "independent-recovery", millis(1),
          () => {
            Thread.sleep(10)
            if (successCount.get() < 1) { errorCount.incrementAndGet(); throw new Exception("error") }
            successCount.incrementAndGet()
          },
          shouldFireCheckIntervalMillis = 1, concurrency = 0
        )
        Thread.sleep(50)
        errorCount.get should be > 0
        successCount.set(1)
        Wait.until(successCount.get >= 2, timeoutMs = 1000, retryIntervalMs = 10)
        scheduler.shutdown()
      }
    }

    "suspend and unsuspend" - {
      "GlobalIntervalScheduler" in {
        val schedulerName = "global-suspend"

        resetSchedulerRow(schedulerName)

        val executionCount = new AtomicInteger(0)
        val scheduler = GlobalIntervalScheduler(
          KoskiApplicationForTests, schedulerName, millis(1),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 1
        )
        Wait.until(executionCount.get >= 2, timeoutMs = 1000)
        scheduler.suspend()
        Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)
        val countAfterSuspend = executionCount.get
        Thread.sleep(200)
        executionCount.get should equal(countAfterSuspend)
        scheduler.unsuspend()
        Wait.until(executionCount.get > countAfterSuspend, timeoutMs = 1000)
        scheduler.shutdown()
      }

      "IndependentIntervalScheduler" in {
        val executionCount = new AtomicInteger(0)
        val scheduler = IndependentIntervalScheduler(
          KoskiApplicationForTests, "independent-suspend", millis(1),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 1, concurrency = 0
        )
        Wait.until(executionCount.get >= 2, timeoutMs = 1000)
        scheduler.suspend()
        Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)
        val countAfterSuspend = executionCount.get
        Thread.sleep(200)
        executionCount.get should equal(countAfterSuspend)
        scheduler.unsuspend()
        Wait.until(executionCount.get > countAfterSuspend, timeoutMs = 1000)
        scheduler.shutdown()
      }
    }

    "does not fire without lease" - {
      "GlobalIntervalScheduler" in {
        val schedulerName = "global-no-lease"

        resetSchedulerRow(schedulerName)

        val executionCount = new AtomicInteger(0)
        val lease = new ControllableLeaseElector
        lease.leaseHeld = false
        val scheduler = GlobalIntervalScheduler.withLeaseElectorOverrideForTests(
          KoskiApplicationForTests, schedulerName, millis(100),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 10,
          leaseElectorOverrideForTests = lease
        )
        Thread.sleep(500)
        executionCount.get should equal(0)
        scheduler.shutdown()
      }

      "IndependentIntervalScheduler" in {
        val executionCount = new AtomicInteger(0)
        val lease = new ControllableLeaseElector
        lease.leaseHeld = false
        val scheduler = IndependentIntervalScheduler.withLeaseElectorOverrideForTests(
          KoskiApplicationForTests, "independent-no-lease", millis(100),
          () => executionCount.incrementAndGet(),
          shouldFireCheckIntervalMillis = 10,
          concurrency = 2,
          leaseElectorOverrideForTests = lease
        )
        Thread.sleep(500)
        executionCount.get should equal(0)
        scheduler.shutdown()
      }
    }
  }

  "GlobalIntervalScheduler" - {
    "lease handover: new holder respects DB nextFireTime" in {
      val schedulerName = "global-handover"

      resetSchedulerRow(schedulerName)

      val executionCountA = new AtomicInteger(0)
      val executionCountB = new AtomicInteger(0)
      val leaseA = new ControllableLeaseElector
      val leaseB = new ControllableLeaseElector

      leaseA.leaseHeld = true
      leaseB.leaseHeld = false

      val interval = millis(1000)

      val schedulerA = GlobalIntervalScheduler.withLeaseElectorOverrideForTests(
        KoskiApplicationForTests, schedulerName, interval,
        executionCountA.incrementAndGet,
        shouldFireCheckIntervalMillis = 50,
        leaseElectorOverrideForTests = leaseA
      )

      val schedulerB = GlobalIntervalScheduler.withLeaseElectorOverrideForTests(
        KoskiApplicationForTests, schedulerName, interval,
        executionCountB.incrementAndGet,
        shouldFireCheckIntervalMillis = 50,
        leaseElectorOverrideForTests = leaseB
      )

      Wait.until(executionCountA.get >= 1, timeoutMs = 5000)

      leaseA.leaseHeld = false
      leaseB.leaseHeld = true

      val countAfterTransfer = executionCountB.get
      Thread.sleep(400)
      executionCountB.get should equal(countAfterTransfer)

      Wait.until(executionCountB.get > countAfterTransfer, timeoutMs = 3000)

      schedulerA.shutdown()
      schedulerB.shutdown()
    }

    "triggerNow resets nextFireTime and task fires soon" in {
      val schedulerName = "global-trigger-now"

      resetSchedulerRow(schedulerName)

      val executionCount = new AtomicInteger(0)
      val scheduler = GlobalIntervalScheduler(
        KoskiApplicationForTests, schedulerName, Duration.ofHours(1),
        () => executionCount.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50
      )

      // Long interval, fresh row — should not fire on its own
      Thread.sleep(500)
      executionCount.get should equal(0)

      scheduler.triggerNow()
      Wait.until(executionCount.get >= 1, timeoutMs = 1000)

      scheduler.shutdown()
    }

    "recovers if scheduler row is deleted while running" in {
      val schedulerName = "global-row-deleted"

      resetSchedulerRow(schedulerName)

      val executionCount = new AtomicInteger(0)
      val scheduler = GlobalIntervalScheduler(
        KoskiApplicationForTests, schedulerName, millis(1),
        () => executionCount.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50
      )

      Wait.until(executionCount.get >= 1, timeoutMs = 5000)

      // Delete the scheduler row while the scheduler is running
      resetSchedulerRow(schedulerName)

      val countAfterDelete = executionCount.get

      // Scheduler re-creates the row and resumes firing
      Wait.until(executionCount.get > countAfterDelete, timeoutMs = 5000)

      scheduler.shutdown()
    }

    "setting nextFireTime far in the future stops the scheduler" in {
      val schedulerName = "global-disabled-by-nextfiretime"

      resetSchedulerRow(schedulerName)

      val executionCount = new AtomicInteger(0)
      val scheduler = GlobalIntervalScheduler(
        KoskiApplicationForTests, schedulerName, millis(1),
        () => executionCount.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50
      )

      Wait.until(executionCount.get >= 1, timeoutMs = 5000)

      // Set nextFireTime to year 9999 — scheduler should stop firing
      import java.sql.Timestamp
      val farFuture = Timestamp.valueOf("9999-12-31 23:59:59")
      QueryMethods.runDbSync(KoskiApplicationForTests.masterDatabase.db,
        KoskiTables.Scheduler.filter(_.name === schedulerName).map(_.nextFireTime).update(farFuture))

      val countAfterDisable = executionCount.get
      Thread.sleep(500)
      executionCount.get should equal(countAfterDisable)

      scheduler.shutdown()

      // Even a new scheduler instance with the same name should not fire,
      // because the DB row still has the far-future nextFireTime (ON CONFLICT DO NOTHING)
      val executionCount2 = new AtomicInteger(0)
      val scheduler2 = GlobalIntervalScheduler(
        KoskiApplicationForTests, schedulerName, millis(1),
        () => executionCount2.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50
      )

      Thread.sleep(500)
      executionCount2.get should equal(0)

      scheduler2.shutdown()
    }

    "withContext" - {
      "context persists between firings" in {
        val schedulerName = "global-context-persistence"

        resetSchedulerRow(schedulerName)

        val receivedContexts = new AtomicReference[List[Option[JValue]]](List.empty)

        val scheduler = GlobalIntervalScheduler.withContext(
          KoskiApplicationForTests,
          schedulerName,
          millis(1),
          JInt(0),
          context => {
            receivedContexts.getAndUpdate(list => list :+ context)
            val counter = context.collect { case JInt(n) => n.toInt }.getOrElse(0)
            Some(JInt(counter + 1))
          },
          shouldFireCheckIntervalMillis = 1
        )

        Wait.until(receivedContexts.get.size >= 2, timeoutMs = 5000)

        val contexts = receivedContexts.get
        contexts(0) should equal(Some(JInt(0)))
        contexts(1) should equal(Some(JInt(1)))

        scheduler.shutdown()
      }

      "restart continues from DB context, not initialContext" in {
        val schedulerName = "global-context-restart"

        resetSchedulerRow(schedulerName)

        val receivedContexts = new AtomicReference[List[Option[JValue]]](List.empty)

        val scheduler1 = GlobalIntervalScheduler.withContext(
          KoskiApplicationForTests,
          schedulerName,
          millis(1),
          JInt(0),
          context => {
            receivedContexts.getAndUpdate(list => list :+ context)
            val counter = context.collect { case JInt(n) => n.toInt }.getOrElse(0)
            Some(JInt(counter + 1))
          },
          shouldFireCheckIntervalMillis = 1
        )

        Wait.until(receivedContexts.get.exists(_.contains(JInt(2))), timeoutMs = 5000)
        scheduler1.shutdown()

        // Second scheduler with same name, initialContext = 0
        // Should continue from DB context, not from initialContext
        val receivedContexts2 = new AtomicReference[List[Option[JValue]]](List.empty)

        val scheduler2 = GlobalIntervalScheduler.withContext(
          KoskiApplicationForTests,
          schedulerName,
          millis(1),
          JInt(0),
          context => {
            receivedContexts2.getAndUpdate(list => list :+ context)
            val counter = context.collect { case JInt(n) => n.toInt }.getOrElse(0)
            Some(JInt(counter + 1))
          },
          shouldFireCheckIntervalMillis = 1
        )

        Wait.until(receivedContexts2.get.nonEmpty, timeoutMs = 5000)

        val firstContextAfterRestart = receivedContexts2.get.head
        firstContextAfterRestart.get match {
          case JInt(n) => n.toInt should be > 0
          case other => fail(s"Unexpected context: $other")
        }

        scheduler2.shutdown()
      }
    }
  }

  "IndependentIntervalScheduler" - {
    "multiple lease holders fire concurrently" in {
      val executionCountA = new AtomicInteger(0)
      val executionCountB = new AtomicInteger(0)
      val leaseA = new ControllableLeaseElector
      val leaseB = new ControllableLeaseElector

      leaseA.leaseHeld = true
      leaseB.leaseHeld = true

      val interval = millis(2000)

      val start = System.currentTimeMillis()

      val schedulerA = IndependentIntervalScheduler.withLeaseElectorOverrideForTests(
        KoskiApplicationForTests, "independent-parallel", interval,
        () => executionCountA.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50,
        concurrency = 2,
        leaseElectorOverrideForTests = leaseA
      )

      val schedulerB = IndependentIntervalScheduler.withLeaseElectorOverrideForTests(
        KoskiApplicationForTests, "independent-parallel", interval,
        () => executionCountB.incrementAndGet(),
        shouldFireCheckIntervalMillis = 50,
        concurrency = 2,
        leaseElectorOverrideForTests = leaseB
      )

      Wait.until( (executionCountA.get + executionCountB.get) >= 2, timeoutMs = 1500)

      (System.currentTimeMillis() - start) should be <(2000L)

      Wait.until( (executionCountA.get + executionCountB.get) >= 4, timeoutMs = 3000)

      (System.currentTimeMillis() - start) should be >=(2000L)
      (System.currentTimeMillis() - start) should be <(4000L)

      schedulerA.shutdown()
      schedulerB.shutdown()
    }

    "concurrency=0: fires without lease coordination" in {
      val executionCount = new AtomicInteger(0)
      val scheduler = IndependentIntervalScheduler(
        KoskiApplicationForTests, "independent-no-lease-all-nodes", millis(1),
        () => executionCount.incrementAndGet(),
        shouldFireCheckIntervalMillis = 1, concurrency = 0
      )

      Wait.until(executionCount.get >= 2, timeoutMs = 5000)
      scheduler.shutdown()
    }
  }
}

/** Test helper: WorkerLeaseElector with externally controllable hasLease.
 * start() and shutdown() are no-ops — no background threads or DB interaction. */
private class ControllableLeaseElector extends WorkerLeaseElector(
  KoskiApplicationForTests.workerLeaseRepository,
  "test-controllable",
  "test-holder",
  slots = 1,
  leaseDuration = Duration.ofHours(1),
  heartbeatInterval = Duration.ofHours(1)
) {
  @volatile var leaseHeld = false
  override def hasLease: Boolean = leaseHeld
  override def start(onAcquired: Int => Unit, onLost: Int => Unit): Unit = ()
  override def shutdown(): Unit = ()
}
