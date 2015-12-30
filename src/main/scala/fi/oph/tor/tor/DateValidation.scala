package fi.oph.tor.tor

import java.time.LocalDate
import fi.oph.tor.http.HttpStatus

object DateValidation {
  type NamedDates = (String, Iterable[LocalDate])

  def validateDateOrder(first: NamedDates, second: NamedDates): HttpStatus = {
    HttpStatus.fold(for (left <- first._2; right <- second._2) yield {
      HttpStatus.validate(left.compareTo(right) <= 0)(HttpStatus.badRequest(first._1 + " (" + left + ") oltava sama tai aiempi kuin " + second._1 + "(" + right + ")"))
    })
  }
}
