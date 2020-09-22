package fi.oph.koski.util

import java.sql.Date
import java.time.LocalDate

object SQL {
  def toSqlListUnsafe[T](xs: Iterable[T]) = xs.mkString("'", "','","'")
  def toSqlDate(d: LocalDate) = Date.valueOf(d)
}
