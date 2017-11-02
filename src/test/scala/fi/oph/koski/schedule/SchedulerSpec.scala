package fi.oph.koski.schedule

import java.time.Duration.{ofMillis => millis}
import java.time.LocalDateTime.now
import java.util.concurrent.atomic.AtomicInteger

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.util.Wait
import org.json4s.JValue
import org.scalatest.{FreeSpec, Matchers}

class SchedulerSpec extends FreeSpec with Matchers {
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

    val s = new Scheduler(KoskiApplicationForTests.masterDatabase.db, "test-sync", new IntervalSchedule(millis(1)), None, longRunningTask, runOnSingleNode = false, intervalMillis = 1)
    val start = System.currentTimeMillis
    Wait.until(sharedResource.get == 1, timeoutMs = 500)
    (System.currentTimeMillis() - start >= 100) should be(true)
    Wait.until(sharedResource.get == 2, timeoutMs = 500)
    (System.currentTimeMillis() - start >= 200) should be(true)
    s.shutdown
  }
}
