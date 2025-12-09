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

    val scheduler = testScheduler(longRunningTask)
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
      val s = testScheduler(failingTask)
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

  private def testScheduler(task: Option[JValue] => Option[JValue]) = {
    new Scheduler(KoskiApplicationForTests.masterDatabase.db, "test", new IntervalSchedule(millis(1)), None, task, runOnSingleNode = false, intervalMillis = 1, KoskiApplicationForTests.config)
  }

  "Single node scheduler" - {
    val db = KoskiApplicationForTests.masterDatabase.db

    def singleNodeScheduler(name: String, schedule: IntervalSchedule, task: Option[JValue] => Option[JValue], intervalMillis: Int = 10): Scheduler = {
      new Scheduler(db, name, schedule, None, task, runOnSingleNode = true, intervalMillis, KoskiApplicationForTests.config)
    }

    "pauseForDuration and resume" - {

      "pauses and resumes scheduler" in {
        val executionCount = new AtomicInteger(0)
        val scheduler = singleNodeScheduler(
          "test-pause-not-running",
          new IntervalSchedule(millis(100)),
          _ => { executionCount.incrementAndGet(); None }
        )

        Wait.until(executionCount.get >= 2, timeoutMs = 1000)

        val paused = Scheduler.pauseForDuration(db, "test-pause-not-running", java.time.Duration.ofSeconds(1))
        paused should be(true)
        val countAfterPause = executionCount.get

        Thread.sleep(500)
        executionCount.get should equal(countAfterPause) // Should not execute during pause

        Thread.sleep(600) // Wait for pause to end
        Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000) // Should resume

        scheduler.shutdown
      }

      "clears pause when new Scheduler is created with same name" in {
        val executionCount = new AtomicInteger(0)
        val scheduler1 = singleNodeScheduler(
          "test-pause-restart",
          new IntervalSchedule(millis(100)),
          _ => { executionCount.incrementAndGet(); None }
        )

        Wait.until(executionCount.get >= 1, timeoutMs = 1000)

        // Pause for a long time
        val paused = Scheduler.pauseForDuration(db, "test-pause-restart", java.time.Duration.ofMinutes(10))
        paused should be(true)
        val countAfterPause = executionCount.get

        Thread.sleep(300)
        executionCount.get should equal(countAfterPause) // Should not execute during pause

        // Create new scheduler with same name (simulates container restart)
        // This should clear the pause
        val scheduler2 = singleNodeScheduler(
          "test-pause-restart",
          new IntervalSchedule(millis(100)),
          _ => { executionCount.incrementAndGet(); None }
        )

        // Should start executing again despite the previous 10 minute pause
        Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000)

        scheduler1.shutdown
        scheduler2.shutdown
      }

      "waits for running task to complete when pausing" in {
        val taskStarted = new AtomicInteger(0)
        val taskCompleted = new AtomicInteger(0)

        val scheduler = singleNodeScheduler(
          "test-pause-running",
          new IntervalSchedule(millis(50)),
          _ => {
            taskStarted.incrementAndGet()
            Thread.sleep(2000) // Long running task
            taskCompleted.incrementAndGet()
            None
          }
        )

        Wait.until(taskStarted.get >= 1, timeoutMs = 1000)
        scheduler.isTaskRunning should be(true) // Task still running
        taskCompleted.get should equal(0)

        val startTime = System.currentTimeMillis()
        val paused = Scheduler.pauseForDuration(db, "test-pause-running", java.time.Duration.ofSeconds(5))
        val elapsed = System.currentTimeMillis() - startTime

        paused should be(true)
        scheduler.isTaskRunning should be(false) // Task completed
        taskCompleted.get should equal(1)
        elapsed should be >= 1500L // Should have waited for task to complete
        elapsed should be < 3000L // But not hung

        scheduler.shutdown
      }

      "stops waiting if resume is called" in {
        val taskStarted = new AtomicInteger(0)

        val scheduler = singleNodeScheduler(
          "test-pause-resume",
          new IntervalSchedule(millis(50)),
          _ => {
            taskStarted.incrementAndGet()
            Thread.sleep(5000) // Very long running task
            None
          }
        )

        Wait.until(taskStarted.get >= 1, timeoutMs = 1000)

        val pauseThread = new Thread(() => {
          Scheduler.pauseForDuration(db, "test-pause-resume", java.time.Duration.ofMinutes(10))
        })

        val startTime = System.currentTimeMillis()
        pauseThread.start()
        Thread.sleep(500) // Give pause time to start waiting

        Scheduler.resume(db, "test-pause-resume")
        pauseThread.join(2000) // Should return quickly after resume

        val elapsed = System.currentTimeMillis() - startTime
        elapsed should be < 3000L // Should not wait the full 5s for task

        scheduler.shutdown
      }

      "stops waiting when pause duration ends" in {
        val taskStarted = new AtomicInteger(0)

        val scheduler = singleNodeScheduler(
          "test-pause-duration-ends",
          new IntervalSchedule(millis(50)),
          _ => {
            taskStarted.incrementAndGet()
            Thread.sleep(5000) // Very long running task
            None
          }
        )

        Wait.until(taskStarted.get >= 1, timeoutMs = 1000)

        val startTime = System.currentTimeMillis()
        val paused = Scheduler.pauseForDuration(db, "test-pause-duration-ends", java.time.Duration.ofSeconds(1))
        val elapsed = System.currentTimeMillis() - startTime

        paused should be(true)
        elapsed should be >= 1000L // Should wait at least 1s (pause duration)
        elapsed should be < 3000L // Should not wait for full 5s task completion

        scheduler.shutdown
      }

      "throws timeout if pausing runs too long" in {
        Scheduler.setPauseTimeoutForTests(3000L) // 3 seconds timeout for testing

        try {
          val taskStarted = new AtomicInteger(0)

          val scheduler = singleNodeScheduler(
            "test-pause-timeout",
            new IntervalSchedule(millis(50)),
            _ => {
              taskStarted.incrementAndGet()
              Thread.sleep(10000) // 10 second task
              None
            }
          )

          Wait.until(taskStarted.get >= 1, timeoutMs = 1000)

          val exception = intercept[RuntimeException] {
            Scheduler.pauseForDuration(db, "test-pause-timeout", java.time.Duration.ofMinutes(10))
          }

          exception.getMessage should include("Timeout waiting for scheduler")
          exception.getMessage should include("3000")

          scheduler.shutdown
        } finally {
          Scheduler.resetPauseTimeout()
        }
      }
    }
  }


  "Multi node scheduler, that does not track running status in database" - {
    // Multi-node -schedulerissa seuraava laukaisuaika ja tieto siitä, onko task tällä hetkellä ajossa, on vain muistissa,
    // vaikka joitain muita tietokannan kenttiä käytetäänkin. Tämän vuoksi sen pause+resume -logiikkalle on rajoitetummat
    // testit.
    val db = KoskiApplicationForTests.masterDatabase.db

    def multiNodeScheduler(name: String, schedule: IntervalSchedule, task: Option[JValue] => Option[JValue], intervalMillis: Int = 10): Scheduler = {
      new Scheduler(db, name, schedule, None, task, runOnSingleNode = false, intervalMillis, KoskiApplicationForTests.config)
    }

    "pauseForDuration and resume" - {

      "pauses and resumes scheduler" in {
        val executionCount = new AtomicInteger(0)
        val scheduler = multiNodeScheduler(
          "test-pause-not-running",
          new IntervalSchedule(millis(100)),
          _ => {
            executionCount.incrementAndGet(); None
          }
        )

        Wait.until(executionCount.get >= 2, timeoutMs = 1000)

        val paused = Scheduler.pauseForDuration(db, "test-pause-not-running", java.time.Duration.ofSeconds(1))
        paused should be(true)
        val countAfterPause = executionCount.get

        Thread.sleep(500)
        executionCount.get should equal(countAfterPause) // Should not execute during pause

        Thread.sleep(600) // Wait for pause to end
        Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000) // Should resume

        scheduler.shutdown
      }

      "clears pause when new Scheduler is created with same name" in {
        val executionCount = new AtomicInteger(0)
        val scheduler1 = multiNodeScheduler(
          "test-multi-pause-restart",
          new IntervalSchedule(millis(100)),
          _ => { executionCount.incrementAndGet(); None }
        )

        Wait.until(executionCount.get >= 1, timeoutMs = 1000)

        // Pause for a long time
        val paused = Scheduler.pauseForDuration(db, "test-multi-pause-restart", java.time.Duration.ofMinutes(10))
        paused should be(true)
        val countAfterPause = executionCount.get

        Thread.sleep(300)
        executionCount.get should equal(countAfterPause) // Should not execute during pause

        // Create new scheduler with same name (simulates container restart)
        // This should clear the pause
        val scheduler2 = multiNodeScheduler(
          "test-multi-pause-restart",
          new IntervalSchedule(millis(100)),
          _ => { executionCount.incrementAndGet(); None }
        )

        // Should start executing again despite the previous 10 minute pause
        Wait.until(executionCount.get > countAfterPause, timeoutMs = 1000)

        scheduler1.shutdown
        scheduler2.shutdown
      }

      "supports waiting for running task to complete, when actually running on single node" in {
        // Tämä on vain testikoodia varten: koska multi-node -skedulerissa muiden konttien skedulereiden
        // run-statukseen ei ole keinoa päästä käsiksi, koska ovat vain kyseisten konttien muistissa, eivätkä
        // tietokannassa.
        val taskStarted = new AtomicInteger(0)
        val taskCompleted = new AtomicInteger(0)

        val scheduler = multiNodeScheduler(
          "test-multi-pause-running",
          new IntervalSchedule(millis(50)),
          _ => {
            taskStarted.incrementAndGet()
            Thread.sleep(2000) // Long running task
            taskCompleted.incrementAndGet()
            None
          }
        )

        Wait.until(taskStarted.get >= 1, timeoutMs = 1000)
        scheduler.isTaskRunning should be(true) // Task running on this node
        taskCompleted.get should equal(0)

        val paused = Scheduler.pauseForDuration(db, "test-multi-pause-running", java.time.Duration.ofSeconds(5))
        paused should be(true)

        // For multi-node scheduler, pauseForDuration doesn't wait for this node's local task to complete
        // Must wait separately using isTaskRunning
        Wait.until(!scheduler.isTaskRunning, timeoutMs = 3000)

        scheduler.isTaskRunning should be(false) // Task completed on this node
        taskCompleted.get should equal(1)

        scheduler.shutdown
      }
    }
  }
}
