package fi.oph.koski.util

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZonedDateTime}

object TimeConversions {
  def toLocalDateTime(time: Timestamp): LocalDateTime =
    time.toLocalDateTime

  def toZonedDateTime(time: LocalDateTime): ZonedDateTime =
    ZonedDateTime.of(time, ZonedDateTime.now().getZone)

  def toTimestamp(dateTime: ZonedDateTime): Timestamp = {
    // JDK11 muuttaa ZonedDateTimen toiminnallisuutta, koska se käyttää tarkempaa kelloa kuin JDK8.
    // Tämän takia nanosekunnit on lisättävä aikaleimaan erikseen.
    // https://bugs.openjdk.org/browse/JDK-8068730
    val epochMillis = dateTime.toInstant.toEpochMilli
    val nanos = dateTime.getNano % 1000000
    Timestamp.from(Instant.ofEpochMilli(epochMillis).plusNanos(nanos))
  }
}
