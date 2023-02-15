package fi.oph.koski.util

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.time.{ZoneId, ZonedDateTime}

class TimeConversionSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "TimeConversionSpec" - {
    "toTimestamp" in {
      val zonedTime = ZonedDateTime.of(2022, 1, 1, 10, 55, 45, 558, ZoneId.systemDefault())
      val tstamp = Timestamp.from(zonedTime.toInstant)
      TimeConversions.toTimestamp(zonedTime) shouldEqual tstamp
    }
    "toZonedDateTime" in {
      val zonedTime = ZonedDateTime.of(2022, 1, 1, 10, 55, 45, 558, ZoneId.systemDefault())
      val localTime = zonedTime.toLocalDateTime
      TimeConversions.toZonedDateTime(localTime) shouldEqual zonedTime
    }
    "toLocalDateTime" in {
      val zonedTime = ZonedDateTime.of(2022, 1, 1, 10, 55, 45, 558, ZoneId.systemDefault())
      val localTime = zonedTime.toLocalDateTime
      TimeConversions.toLocalDateTime(Timestamp.from(zonedTime.toInstant)) shouldEqual localTime
    }
  }
}
