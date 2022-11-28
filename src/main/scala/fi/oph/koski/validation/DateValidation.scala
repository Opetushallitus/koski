package fi.oph.koski.validation

import java.time.LocalDate
import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeusjakso}

object DateValidation {
  type NamedDates = (String, Iterable[LocalDate])

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

  def validateOpiskeluoikeudenPäivämäärät(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    def formatOptionalDate(date: Option[LocalDate]) = date match {
      case Some(d) => d.toString
      case None => "null"
    }

    val ensimmäisenJaksonPäivä: Option[LocalDate] = opiskeluoikeus.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
    val päätasonSuorituksenAlkamispäivät = opiskeluoikeus.suoritukset.flatMap(_.alkamispäivä)

    HttpStatus.fold(
      validateDateOrder(
        ("alkamispäivä", opiskeluoikeus.alkamispäivä),
        ("päättymispäivä", opiskeluoikeus.päättymispäivä),
        KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää
      ),
      validateDateOrder(
        ("alkamispäivä", opiskeluoikeus.alkamispäivä),
        ("arvioituPäättymispäivä", opiskeluoikeus.arvioituPäättymispäivä),
        KoskiErrorCategory.badRequest.validation.date.arvioituPäättymisPäiväEnnenAlkamispäivää
      ),
      validateDateOrder(
        ("opiskeluoikeuden ensimmäisen tilan alkamispäivä", opiskeluoikeus.alkamispäivä),
        ("päätason suorituksen alkamispäivä", päätasonSuorituksenAlkamispäivät),
        KoskiErrorCategory.badRequest.validation.date.suorituksenAlkamispäiväEnnenOpiskeluoikeudenAlkamispäivää
      ),
      validateJaksotPäättyminen(opiskeluoikeus.tila.opiskeluoikeusjaksot),
      DateValidation.validateJaksotDateOrder(
        "tila.opiskeluoikeusjaksot",
        opiskeluoikeus.tila.opiskeluoikeusjaksot,
        KoskiErrorCategory.badRequest.validation.date.opiskeluoikeusjaksojenPäivämäärät
      ),
      HttpStatus.validate(
        opiskeluoikeus.alkamispäivä == ensimmäisenJaksonPäivä
      )(
        KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Opiskeluoikeuden alkamispäivä (${formatOptionalDate(opiskeluoikeus.alkamispäivä)}) ei vastaa ensimmäisen opiskeluoikeusjakson alkupäivää (${formatOptionalDate(ensimmäisenJaksonPäivä)})")
      )
    )
  }

  private def validateJaksotPäättyminen(jaksot: List[Opiskeluoikeusjakso]) = {
    jaksot.filter(_.opiskeluoikeusPäättynyt) match {
      case Nil => HttpStatus.ok
      case List(päättäväJakso) => HttpStatus.validate(jaksot.last.opiskeluoikeusPäättynyt)(KoskiErrorCategory.badRequest.validation.tila.tilaMuuttunutLopullisenTilanJälkeen(s"Opiskeluoikeuden tila muuttunut lopullisen tilan (${päättäväJakso.tila.koodiarvo}) jälkeen"))
      case List(_, _) => HttpStatus.validate(jaksot.last.tila.koodiarvo == "mitatoity")(KoskiErrorCategory.badRequest.validation.tila.montaPäättävääTilaa(s"Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila"))
      case _ => KoskiErrorCategory.badRequest.validation.tila.montaPäättävääTilaa(s"Opiskeluoikeudella voi olla vain yksi opiskeluoikeuden päättävä tila")
    }
  }
}
