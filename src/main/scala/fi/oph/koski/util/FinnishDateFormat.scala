package fi.oph.koski.util

import java.time.format.DateTimeFormatter

object FinnishDateFormat {
  val finnishDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy");
}
