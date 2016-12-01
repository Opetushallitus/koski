package fi.oph.koski.date

import java.time.{LocalDate, LocalDateTime}

object DateOrdering {
  implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)
  implicit def localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)
  def localDateTimeReverseOrdering: Ordering[LocalDateTime] = localDateTimeOrdering.reverse
}
