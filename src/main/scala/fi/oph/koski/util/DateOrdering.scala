package fi.oph.koski.util

import java.time.LocalDate

object DateOrdering {
  implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)
}
