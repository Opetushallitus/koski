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
    new Scheduler(KoskiApplicationForTests.masterDatabase.db, "test", new IntervalSchedule(millis(1)), None, task, runOnSingleNode = false, intervalMillis = 1)
  }
}
