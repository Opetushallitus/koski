package fi.oph.koski.schedule

import java.time.Duration.{ofMillis => millis}
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.util.Wait
import org.json4s.{JValue, JInt}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration

class SchedulerSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Scheduler doesn't run if previous is active" in {
    val sharedResource: AtomicInteger = new AtomicInteger(0)
    def longRunningTask() = {
      Thread.sleep(100)
      sharedResource.incrementAndGet()
    }

    val scheduler = testScheduler("test-no-overlap", longRunningTask)
    val start = System.currentTimeMillis
    Wait.until(sharedResource.get == 1, timeoutMs = 5000)
    (System.currentTimeMillis() - start >= 100) should be(true)
    Wait.until(sharedResource.get == 2, timeoutMs = 5000)
    (System.currentTimeMillis() - start >= 200) should be(true)
    scheduler.shutdown()
  }

  "Scheduler" - {
    val sharedResource: AtomicInteger = new AtomicInteger(0)

    "recovers from errors" in {
      val s = testScheduler("test-recovery", failingTask)
      Thread.sleep(50)
      fixScheduler
      schedulerShouldRecover
      s.shutdown()
    }

    def failingTask() = {
      Thread.sleep(10)
      if (sharedResource.get() < 1) {
        throw new Exception("error")
      }
      sharedResource.incrementAndGet()
    }

    def fixScheduler = sharedResource.set(1)
    def schedulerShouldRecover = Wait.until(sharedResource.get == 2, timeoutMs = 1000, retryIntervalMs = 10)
  }

  "suspend and unsuspend" in {
    val executionCount = new AtomicInteger(0)
    val scheduler = testScheduler(
      "test-suspend",
      () => { executionCount.incrementAndGet() }
    )

    Wait.until(executionCount.get >= 2, timeoutMs = 1000)

    scheduler.suspend()
    Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)
    val countAfterSuspend = executionCount.get

    Thread.sleep(200)
    executionCount.get should equal(countAfterSuspend) // Should not execute while suspended

    scheduler.unsuspend()
    Wait.until(executionCount.get > countAfterSuspend, timeoutMs = 1000) // Should resume

    scheduler.shutdown()
  }

  "independentWorkers" - {
    "multiple lease holders fire concurrently when independentWorkers is enabled" in {
      val executionCountA = new AtomicInteger(0)
      val executionCountB = new AtomicInteger(0)
      val leaseA = new ControllableLeaseElector
      val leaseB = new ControllableLeaseElector

      leaseA.leaseHeld = true
      leaseB.leaseHeld = true

      // Long interval: without independentWorkers, the first to fire would push
      // nextFireTime 2s into the future, blocking the other from firing.
      val interval = Duration.ofMillis(2000)

      // Multiple containers are simulated by multiple Scheduler objects with same name.
      val schedulerA = Scheduler(
        KoskiApplicationForTests, "test-independent-workers", new IntervalSchedule(interval),
        () => { executionCountA.incrementAndGet() },
        intervalMillis = 50,
        mode = LeaseControlledWithIndependentSchedules(2, leaseElectorOverrideForTests = Some(leaseA))
      )

      val schedulerB = Scheduler(
        KoskiApplicationForTests, "test-independent-workers", new IntervalSchedule(interval),
        () => { executionCountB.incrementAndGet() },
        intervalMillis = 50,
        mode = LeaseControlledWithIndependentSchedules(2, leaseElectorOverrideForTests = Some(leaseB))
      )

      // Both should fire within a short window — not blocked by the 2s interval
      Wait.until(executionCountA.get >= 1, timeoutMs = 1500)
      Wait.until(executionCountB.get >= 1, timeoutMs = 1500)

      schedulerA.shutdown()
      schedulerB.shutdown()
    }

    "lease holder without lease does not fire even with independentWorkers" in {
      val executionCount = new AtomicInteger(0)
      val lease = new ControllableLeaseElector

      lease.leaseHeld = false

      val scheduler = Scheduler(
        KoskiApplicationForTests, "test-independent-no-lease", new IntervalSchedule(Duration.ofMillis(100)),
        () => { executionCount.incrementAndGet() },
        intervalMillis = 10,
        mode = LeaseControlledWithIndependentSchedules(2, leaseElectorOverrideForTests = Some(lease))
      )

      Thread.sleep(500)
      executionCount.get should equal(0)

      scheduler.shutdown()
    }
  }

  "lease handover" - {
    "new lease holder respects global cadence from DB, does not fire immediately" in {
      val executionCountA = new AtomicInteger(0)
      val executionCountB = new AtomicInteger(0)
      val leaseA = new ControllableLeaseElector
      val leaseB = new ControllableLeaseElector

      leaseA.leaseHeld = true
      leaseB.leaseHeld = false

      val interval = Duration.ofMillis(1000)

      val schedulerA = Scheduler(
        KoskiApplicationForTests, "test-handover-cadence", new IntervalSchedule(interval),
        () => { executionCountA.incrementAndGet() },
        intervalMillis = 50,
        mode = LeaseControlledWithSharedSchedule(1, leaseElectorOverrideForTests = Some(leaseA))
      )

      val schedulerB = Scheduler(
        KoskiApplicationForTests, "test-handover-cadence", new IntervalSchedule(interval),
        () => { executionCountB.incrementAndGet() },
        intervalMillis = 50,
        mode = LeaseControlledWithSharedSchedule(1, leaseElectorOverrideForTests = Some(leaseB))
      )

      // Wait for A to fire at least once
      Wait.until(executionCountA.get >= 1, timeoutMs = 5000)

      // Transfer lease from A to B
      leaseA.leaseHeld = false
      leaseB.leaseHeld = true

      // B should NOT fire immediately — A just fired and the 1s interval hasn't elapsed
      val countAfterTransfer = executionCountB.get
      Thread.sleep(400)
      executionCountB.get should equal(countAfterTransfer)

      // After the full interval elapses, B should fire
      Wait.until(executionCountB.get > countAfterTransfer, timeoutMs = 3000)

      schedulerA.shutdown()
      schedulerB.shutdown()
    }
  }

  "Scheduler.withContext" - {
    "persists and passes context between firings" in {
      val schedulerName = "test-context-persistence"

      // Reset scheduler row to ensure clean state on repeated runs
      import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
      QueryMethods.runDbSync(KoskiApplicationForTests.masterDatabase.db, sqlu"DELETE FROM scheduler WHERE name = $schedulerName")

      val receivedContexts = new AtomicReference[List[Option[JValue]]](List.empty)

      def task(context: Option[JValue]): Option[JValue] = {
        receivedContexts.getAndUpdate(list => list :+ context)
        val counter = context.collect { case JInt(n) => n.toInt }.getOrElse(0)
        Some(JInt(counter + 1))
      }

      val scheduler = Scheduler.withContext(
        KoskiApplicationForTests,
        schedulerName,
        new IntervalSchedule(millis(1)),
        JInt(0),
        task,
        intervalMillis = 1
      )

      Wait.until(receivedContexts.get.size >= 2, timeoutMs = 5000)

      val contexts = receivedContexts.get
      // First firing receives the initial context
      contexts(0) should equal(Some(JInt(0)))
      // Second firing receives the updated context from DB
      contexts(1) should equal(Some(JInt(1)))

      scheduler.shutdown()
    }
  }

  "SchedulerMode factory fallbacks" - {
    "leaseControlledWithIndependentSchedulesFromConfig with concurrency >= 2 returns LeaseControlledWithIndependentSchedules" in {
      SchedulerMode.leaseControlledWithIndependentSchedules(concurrency = 3) shouldBe a[LeaseControlledWithIndependentSchedules]
    }

    "leaseControlledWithIndependentSchedulesFromConfig with concurrency = 1 falls back to LeaseControlledWithSharedSchedule" in {
      val mode = SchedulerMode.leaseControlledWithIndependentSchedules(concurrency = 1)
      mode shouldBe a[LeaseControlledWithSharedSchedule]
      mode.asInstanceOf[LeaseControlledWithSharedSchedule].concurrency should equal(1)
    }

    "leaseControlledWithIndependentSchedulesFromConfig with concurrency = 0 falls back to RunOnAllNodes" in {
      SchedulerMode.leaseControlledWithIndependentSchedules(concurrency = 0) should equal(RunOnAllNodes)
    }

    "leaseControlledWithIndependentSchedulesFromConfig with negative concurrency falls back to RunOnAllNodes" in {
      SchedulerMode.leaseControlledWithIndependentSchedules(concurrency = -1) should equal(RunOnAllNodes)
    }

    "leaseControlledWithSharedScheduleFromConfig with concurrency >= 1 returns LeaseControlledWithSharedSchedule" in {
      SchedulerMode.leaseControlledWithSharedSchedule(concurrency = 1) shouldBe a[LeaseControlledWithSharedSchedule]
    }

    "leaseControlledWithSharedScheduleFromConfig with concurrency = 0 falls back to RunOnAllNodes" in {
      SchedulerMode.leaseControlledWithSharedSchedule(concurrency = 0) should equal(RunOnAllNodes)
    }

    "leaseControlledWithSharedScheduleFromConfig with negative concurrency falls back to RunOnAllNodes" in {
      SchedulerMode.leaseControlledWithSharedSchedule(concurrency = -1) should equal(RunOnAllNodes)
    }
  }

  private def testScheduler(name: String = "test", task: () => Unit) = {
    Scheduler(KoskiApplicationForTests, name, new IntervalSchedule(millis(1)), task, intervalMillis = 1)
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
