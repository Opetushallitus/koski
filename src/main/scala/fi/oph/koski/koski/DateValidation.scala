package fi.oph.koski.koski

import java.time.LocalDate
import fi.oph.koski.http.{KoskiErrorCategory, HttpStatus}
import fi.oph.koski.schema.Jakso

object DateValidation {
  type NamedDates = (String, Iterable[LocalDate])

  def validateDateOrder(first: NamedDates, second: NamedDates): HttpStatus = {
    HttpStatus.fold(for (left <- first._2; right <- second._2) yield {
      HttpStatus.validate(left.compareTo(right) <= 0)(KoskiErrorCategory.badRequest.validation.date.loppuEnnenAlkua(first._1 + " (" + left + ") oltava sama tai aiempi kuin " + second._1 + "(" + right + ")"))
    })
  }

  def validateJaksot(name: String, jaksot: Iterable[Jakso]): HttpStatus = {
    HttpStatus.fold(jaksot.map { jakso => validateDateOrder((name + ".alku", Some(jakso.alku)), (name + ".loppu", jakso.loppu))})
      .then {
        val pairs = jaksot.zip(jaksot.drop(1)).map {
          case (left, right) => (left.loppu, right.alku)
        }
        HttpStatus.fold(pairs.map {
          case (None, _) => KoskiErrorCategory.badRequest.validation.date.jaksonLoppupäiväPuuttuu(name + ": ei-viimeiseltä jaksolta puuttuu loppupäivä")
          case (Some(edellisenLoppu), seuraavanAlku) if (!areConsecutiveDates(edellisenLoppu, seuraavanAlku)) => KoskiErrorCategory.badRequest.validation.date.jaksotEivätMuodostaJatkumoa(name + ": jaksot eivät muodosta jatkumoa")
          case _ => HttpStatus.ok
        })
      }
  }

  def areConsecutiveDates(edellisenLoppu: LocalDate, seuraavanAlku: LocalDate) = edellisenLoppu.plusDays(1) == seuraavanAlku
}
