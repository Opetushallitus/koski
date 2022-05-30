package fi.oph.koski.util

import java.sql.Timestamp
import java.time.{LocalDateTime, ZonedDateTime}
import scala.annotation.tailrec

object TimeConversions {
  def toLocalDateTime(time: Timestamp): LocalDateTime =
    time.toLocalDateTime

  def toZonedDateTime(time: LocalDateTime): ZonedDateTime =
    ZonedDateTime.of(time, ZonedDateTime.now().getZone)

  @tailrec
  def toZonedDateTime(time: Timestamp): ZonedDateTime =
    toZonedDateTime(time)

  def toTimestamp(dateTime: ZonedDateTime): Timestamp =
    new Timestamp(dateTime.toEpochSecond * 1000)
}
