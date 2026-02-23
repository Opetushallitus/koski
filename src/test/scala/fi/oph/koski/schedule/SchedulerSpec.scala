package fi.oph.koski.schedule

import java.time.Duration.{ofMillis => millis}
import java.time.LocalDateTime.now
import java.util.concurrent.atomic.AtomicInteger
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.util.Wait
import org.json4s.JValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

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

  private def testScheduler(name: String = "test", task: Option[JValue] => Option[JValue]) = {
    new Scheduler(KoskiApplicationForTests.masterDatabase.db, name, new IntervalSchedule(millis(1)), None, task, intervalMillis = 1, KoskiApplicationForTests.config)
  }
}
