package fi.oph.koski.util

import java.time.{LocalDate, LocalDateTime}

object DateOrdering {
  implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)
  implicit def localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)

  // Note: sorts "None" last, opposite of scala.math.Ordering.OptionOrdering
  lazy val localDateOptionOrdering = new Ordering[Option[LocalDate]] {
    override def compare(x: Option[LocalDate], y: Option[LocalDate]) = (x, y) match {
      case (None, Some(_)) => 1
      case (Some(_), None) => -1
      case (None, None) => 0
      case (Some(x), Some(y)) => if (x.isBefore(y)) { -1 } else { 1 }
    }
  }

  lazy val sqlDateOrdering: Ordering[java.sql.Date] = Ordering.fromLessThan(_ before _)
}
