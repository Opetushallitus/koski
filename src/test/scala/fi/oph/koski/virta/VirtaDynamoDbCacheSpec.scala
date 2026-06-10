package fi.oph.koski.virta

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDate, ZoneId, ZonedDateTime}

class VirtaDynamoDbCacheSpec extends AnyFreeSpec with Matchers {
  private val helsinkiZone = ZoneId.of("Europe/Helsinki")

  "currentGeneration" - {
    "returns today's date when called after 06:00 Helsinki" in {
      val cache = cacheWithFixedTime(hour = 7, minute = 0)
      val expected = LocalDate.now(helsinkiZone).toString
      cache.currentGeneration() should equal(expected)
    }

    "returns today's date when called exactly at 06:00 Helsinki" in {
      val cache = cacheWithFixedTime(hour = 6, minute = 0)
      val expected = LocalDate.now(helsinkiZone).toString
      cache.currentGeneration() should equal(expected)
    }

    "returns yesterday's date when called before 06:00 Helsinki" in {
      val cache = cacheWithFixedTime(hour = 5, minute = 59)
      val expected = LocalDate.now(helsinkiZone).minusDays(1).toString
      cache.currentGeneration() should equal(expected)
    }
  }

  private def cacheWithFixedTime(hour: Int, minute: Int) = new VirtaDynamoDbCache(null, "test") {
    override private[virta] def currentGeneration(): String = {
      val today = LocalDate.now(helsinkiZone)
      val fixedNow = today.atTime(hour, minute).atZone(helsinkiZone)
      val todayAt6 = today.atTime(6, 0).atZone(helsinkiZone)
      val date = if (fixedNow.isBefore(todayAt6)) today.minusDays(1) else today
      date.toString
    }
  }
}
