package fi.oph.koski.util

import fi.oph.koski.schema.Aikajakso

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FinnishDateFormat {
  val finnishDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy");
  val finnishDateTimeFormat = DateTimeFormatter.ofPattern("d.M.yyyy H:mm")

  def format(date: LocalDate): String = date.format(finnishDateFormat)
  def format(date: Option[LocalDate]): String = date.map(format).getOrElse("")
  def format(alku: Option[LocalDate], loppu: Option[LocalDate]): String = s"${format(alku)}-${format(loppu)}"
}
