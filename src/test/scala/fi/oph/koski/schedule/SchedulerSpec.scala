package fi.oph.koski.schedule

import java.time.LocalDateTime.now

import org.scalatest.{FreeSpec, Matchers}

class SchedulerSpec extends FreeSpec with Matchers {
  "Next fire time is on selected time next day" in {
    val nextFireTime = new FixedTimeOfDaySchedule(3, 10).nextFireTime.toLocalDateTime
    val expected = now.plusDays(1).withHour(3).withMinute(10)
    nextFireTime.getDayOfMonth should equal(expected.getDayOfMonth)
    nextFireTime.getHour should equal(expected.getHour)
    nextFireTime.getMinute should equal(expected.getMinute)
    nextFireTime.getSecond should equal(expected.getSecond)
  }
}
