package fi.oph.koski.date

import java.time.LocalDate

import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.Alkupäivällinen

object DateValidation {
  type NamedDates = (String, Iterable[LocalDate])

  def validateDateOrder(first: NamedDates, second: NamedDates, errorCategory: ErrorCategory): HttpStatus = {
    HttpStatus.fold(for (left <- first._2; right <- second._2) yield {
      HttpStatus.validate(left.compareTo(right) <= 0)(errorCategory(first._1 + " (" + left + ") oltava sama tai aiempi kuin " + second._1 + "(" + right + ")"))
    })
  }

  def validateJaksot(name: String, jaksot: Iterable[Alkupäivällinen], errorCategory: ErrorCategory): HttpStatus = {
    HttpStatus.fold(jaksot.zip(jaksot.drop(1)).map { case (jakso1, jakso2) =>
      HttpStatus.validate(jakso1.alku.compareTo(jakso2.alku) <= 0)(errorCategory(s"${name}: ${jakso1.alku} oltava sama tai aiempi kuin ${jakso2.alku}"))
    })
  }

  def validateNotInFuture(name: String, errorCategory: ErrorCategory, date: Iterable[LocalDate]): HttpStatus = {
    HttpStatus.fold(date.map(date => validateNotInFuture(name, errorCategory, date)))
  }
  def validateNotInFuture(name: String, errorCategory: ErrorCategory, date: LocalDate): HttpStatus = {
    HttpStatus.validate(!date.isAfter(LocalDate.now)) {
      errorCategory(s"Päivämäärä $name ($date) on tulevaisuudessa")
    }
  }
}
