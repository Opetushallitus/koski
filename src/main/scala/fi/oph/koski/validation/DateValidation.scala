package fi.oph.koski.validation

import java.time.LocalDate

import fi.oph.koski.http.{ErrorCategory, HttpStatus}
import fi.oph.koski.schema.{Opiskeluoikeusjakso}

object DateValidation {
  type NamedDates = (String, Iterable[LocalDate])

   // Turha rivi

  def validateDateOrder(first: NamedDates, second: NamedDates, errorCategory: ErrorCategory): HttpStatus = {
    HttpStatus.fold(for (left <- first._2; right <- second._2) yield {
      HttpStatus.validate(left.compareTo(right) <= 0)(errorCategory(first._1 + " (" + left + ") oltava sama tai aiempi kuin " + second._1 + " (" + right + ")"))
    })
  }

  def validateJaksotDateOrder(name: String, jaksot: Iterable[Opiskeluoikeusjakso], errorCategory: ErrorCategory): HttpStatus = {
    HttpStatus.fold(jaksot.zip(jaksot.drop(1)).map { case (jakso1, jakso2) =>
      val compared = jakso1.alku.compareTo(jakso2.alku)
      if (compared == 0) {
        HttpStatus.validate(jakso2.tila.koodiarvo == "mitatoity")(errorCategory(s"${name}: ${jakso1.tila.koodiarvo} ${jakso1.alku} ei voi olla samalla päivämäärällä kuin ${jakso2.tila.koodiarvo} ${jakso2.alku}"))
      } else {
        HttpStatus.validate(compared < 0)(errorCategory(s"${name}: ${jakso1.alku} on oltava aiempi kuin ${jakso2.alku}"))
      }
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
