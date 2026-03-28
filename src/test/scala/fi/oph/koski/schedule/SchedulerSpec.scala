package fi.oph.koski.schedule

import java.time.Duration.{ofMillis => millis}
import java.time.LocalDateTime.now
import java.util.concurrent.atomic.AtomicInteger
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.util.Wait
import org.json4s.JValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration

class SchedulerSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Next fire time is on selected time next day" in {
    val nextFireTime = new FixedTimeOfDaySchedule(3, 10).nextFireTime().toLocalDateTime
    val expected = now.plusDays(1).withHour(3).withMinute(10)
    nextFireTime.getDayOfMonth should equal(expected.getDayOfMonth)
    nextFireTime.getHour should equal(expected.getHour)
    nextFireTime.getMinute should equal(expected.getMinute)
    nextFireTime.getSecond should equal(expected.getSecond)
  }

  "Scheduler doesn't run if previous is active" in {
    val sharedResource: AtomicInteger = new AtomicInteger(0)
    def longRunningTask(x: Option[JValue]) = {
      Thread.sleep(100)
      sharedResource.incrementAndGet()
      None
    }

    val scheduler = testScheduler("test-no-overlap", longRunningTask)
    val start = System.currentTimeMillis
    Wait.until(sharedResource.get == 1, timeoutMs = 5000)
    (System.currentTimeMillis() - start >= 100) should be(true)
    Wait.until(sharedResource.get == 2, timeoutMs = 5000)
    (System.currentTimeMillis() - start >= 200) should be(true)
    scheduler.shutdown
  }

  "Scheduler" - {
    val sharedResource: AtomicInteger = new AtomicInteger(0)

    "recovers from errors" in {
      val s = testScheduler("test-recovery", failingTask)
      Thread.sleep(50)
      fixScheduler
      schedulerShouldRecover
      s.shutdown
    }

    def failingTask(x: Option[JValue]) = {
      Thread.sleep(10)
      if (sharedResource.get() < 1) {
        throw new Exception("error")
      }
      sharedResource.incrementAndGet()
      None
    }

    def fixScheduler = sharedResource.set(1)
    def schedulerShouldRecover = Wait.until(sharedResource.get == 2, timeoutMs = 1000, retryIntervalMs = 10)
  }

  "suspend and unsuspend" in {
    val executionCount = new AtomicInteger(0)
    val scheduler = testScheduler(
      "test-suspend",
      _ => { executionCount.incrementAndGet(); None }
    )

    Wait.until(executionCount.get >= 2, timeoutMs = 1000)

    scheduler.suspend()
    Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)
    val countAfterSuspend = executionCount.get

    Thread.sleep(200)
    executionCount.get should equal(countAfterSuspend) // Should not execute while suspended

    scheduler.unsuspend()
    Wait.until(executionCount.get > countAfterSuspend, timeoutMs = 1000) // Should resume

    scheduler.shutdown
  }

  "Scheduler with FixedTimeOfDaySchedule doesn't fire immediately on startup" in {
    val executionCount = new AtomicInteger(0)

    val scheduler = new Scheduler(
      KoskiApplicationForTests,
      "test-no-immediate-fire",
      new FixedTimeOfDaySchedule(3, 0),
      None,
      _ => { executionCount.incrementAndGet(); None },
      intervalMillis = 10
    )

    // Wait a bit and verify it didn't fire
    Thread.sleep(200)
    executionCount.get should equal(0)

    scheduler.shutdown
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

      val schedulerA = new Scheduler(
        KoskiApplicationForTests, "test-independent-workers", new IntervalSchedule(interval), None,
        _ => { executionCountA.incrementAndGet(); None },
        intervalMillis = 50,
        independentWorkers = true,
        leaseElectorOverride = Some(leaseA)
      )

      val schedulerB = new Scheduler(
        KoskiApplicationForTests, "test-independent-workers", new IntervalSchedule(interval), None,
        _ => { executionCountB.incrementAndGet(); None },
        intervalMillis = 50,
        independentWorkers = true,
        leaseElectorOverride = Some(leaseB)
      )

      // Both should fire within a short window — not blocked by the 2s interval
      Wait.until(executionCountA.get >= 1, timeoutMs = 1500)
      Wait.until(executionCountB.get >= 1, timeoutMs = 1500)

      schedulerA.shutdown
      schedulerB.shutdown
    }

    "lease holder without lease does not fire even with independentWorkers" in {
      val executionCount = new AtomicInteger(0)
      val lease = new ControllableLeaseElector

      lease.leaseHeld = false

      val scheduler = new Scheduler(
        KoskiApplicationForTests, "test-independent-no-lease", new IntervalSchedule(Duration.ofMillis(100)), None,
        _ => { executionCount.incrementAndGet(); None },
        intervalMillis = 10,
        independentWorkers = true,
        leaseElectorOverride = Some(lease)
      )

      Thread.sleep(500)
      executionCount.get should equal(0)

      scheduler.shutdown
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

      val schedulerA = new Scheduler(
        KoskiApplicationForTests, "test-handover-cadence", new IntervalSchedule(interval), None,
        _ => { executionCountA.incrementAndGet(); None },
        intervalMillis = 50,
        leaseElectorOverride = Some(leaseA)
      )

      val schedulerB = new Scheduler(
        KoskiApplicationForTests, "test-handover-cadence", new IntervalSchedule(interval), None,
        _ => { executionCountB.incrementAndGet(); None },
        intervalMillis = 50,
        leaseElectorOverride = Some(leaseB)
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

      schedulerA.shutdown
      schedulerB.shutdown
    }
  }

  private def testScheduler(name: String = "test", task: Option[JValue] => Option[JValue]) = {
    new Scheduler(KoskiApplicationForTests, name, new IntervalSchedule(millis(1)), None, task, intervalMillis = 1)
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
