package fi.oph.koski.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FinnishDateFormat {
  val finnishDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy");
  val finnishDateTimeFormat = DateTimeFormatter.ofPattern("d.M.yyyy H:mm")

  def format(date: LocalDate): String = date.format(finnishDateFormat)
}
