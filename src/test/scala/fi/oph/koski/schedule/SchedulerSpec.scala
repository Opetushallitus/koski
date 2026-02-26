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

  "pauseForDuration and resume" - {
    val db = KoskiApplicationForTests.masterDatabase.db

    "pauses and resumes scheduler" in {
      val executionCount = new AtomicInteger(0)
      val scheduler = testScheduler(
        "test-pause-not-running",
        _ => { executionCount.incrementAndGet(); None }
      )

      Wait.until(executionCount.get >= 2, timeoutMs = 1000)

      val paused = Scheduler.pauseForDuration(db, "test-pause-not-running", java.time.Duration.ofSeconds(1))
      paused should be(true)
      Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)
      val countAfterPause = executionCount.get

      Thread.sleep(500)
      executionCount.get should equal(countAfterPause) // Should not execute during pause

      Thread.sleep(600) // Wait for pause to end
      Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000) // Should resume

      scheduler.shutdown
    }

    "pauseForDuration is non-blocking and returns while task is still running" in {
      val taskStarted = new java.util.concurrent.CountDownLatch(1)
      val taskCanFinish = new java.util.concurrent.CountDownLatch(1)
      val executionCount = new AtomicInteger(0)

      val scheduler = testScheduler(
        "test-pause-nonblocking",
        _ => {
          taskStarted.countDown()
          taskCanFinish.await(5, java.util.concurrent.TimeUnit.SECONDS)
          executionCount.incrementAndGet()
          None
        }
      )

      // Wait for task to start running
      taskStarted.await(5, java.util.concurrent.TimeUnit.SECONDS)
      scheduler.isTaskRunning should be(true)

      // pauseForDuration should return immediately even though task is still running
      val before = System.currentTimeMillis()
      val paused = Scheduler.pauseForDuration(db, "test-pause-nonblocking", java.time.Duration.ofSeconds(10))
      val elapsed = System.currentTimeMillis() - before
      paused should be(true)
      elapsed should be < 1000L
      scheduler.isTaskRunning should be(true) // task still running

      // Let task finish
      taskCanFinish.countDown()
      Wait.until(!scheduler.isTaskRunning, timeoutMs = 1000)

      scheduler.shutdown
    }

    "clears pause when new Scheduler is created with same name" in {
      val executionCount = new AtomicInteger(0)
      val scheduler1 = testScheduler(
        "test-pause-restart",
        _ => { executionCount.incrementAndGet(); None }
      )

      Wait.until(executionCount.get >= 1, timeoutMs = 1000)

      // Pause for a long time
      val paused = Scheduler.pauseForDuration(db, "test-pause-restart", java.time.Duration.ofMinutes(10))
      paused should be(true)
      Wait.until(!scheduler1.isTaskRunning, timeoutMs = 1000)
      val countAfterPause = executionCount.get

      Thread.sleep(300)
      executionCount.get should equal(countAfterPause) // Should not execute during pause

      // Create new scheduler with same name (simulates container restart)
      // This should clear the pause
      val scheduler2 = testScheduler(
        "test-pause-restart",
        _ => { executionCount.incrementAndGet(); None }
      )

      // Should start executing again despite the previous 10 minute pause
      Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000)

      scheduler1.shutdown
      scheduler2.shutdown
    }
  }

  "Scheduler with FixedTimeOfDaySchedule doesn't fire immediately on startup" in {
    val executionCount = new AtomicInteger(0)
    val db = KoskiApplicationForTests.masterDatabase.db

    val scheduler = new Scheduler(
      db,
      "test-no-immediate-fire",
      new FixedTimeOfDaySchedule(3, 0),
      None,
      _ => { executionCount.incrementAndGet(); None },
      intervalMillis = 10,
      KoskiApplicationForTests.config
    )

    // Wait a bit and verify it didn't fire
    Thread.sleep(200)
    executionCount.get should equal(0)

    scheduler.shutdown
  }

  "lease handover" - {
    val db = KoskiApplicationForTests.masterDatabase.db

    "new lease holder respects global cadence from DB, does not fire immediately" in {
      val executionCountA = new AtomicInteger(0)
      val executionCountB = new AtomicInteger(0)
      val leaseA = new ControllableLeaseElector
      val leaseB = new ControllableLeaseElector

      leaseA.leaseHeld = true
      leaseB.leaseHeld = false

      val interval = Duration.ofMillis(1000)

      val schedulerA = new Scheduler(
        db, "test-handover-cadence", new IntervalSchedule(interval), None,
        _ => { executionCountA.incrementAndGet(); None },
        intervalMillis = 50, KoskiApplicationForTests.config,
        leaseElector = Some(leaseA)
      )

      val schedulerB = new Scheduler(
        db, "test-handover-cadence", new IntervalSchedule(interval), None,
        _ => { executionCountB.incrementAndGet(); None },
        intervalMillis = 50, KoskiApplicationForTests.config,
        leaseElector = Some(leaseB)
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
    new Scheduler(KoskiApplicationForTests.masterDatabase.db, name, new IntervalSchedule(millis(1)), None, task, intervalMillis = 1, KoskiApplicationForTests.config)
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
