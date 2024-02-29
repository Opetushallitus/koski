package fi.oph.koski.util

import fi.oph.koski.json.JsonSerializer
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

object CsvFormatter {
  def linebreak = "\r\n"
  def delimiter = ","

  def formatRecord(fields: Seq[Any]): String =
    fields
      .map(formatField)
      .mkString(delimiter) + linebreak

  def formatField(field: Any): String =
    field match {
      case Some(a: Any) => formatField(a)
      case None => ""
      case s: String => formatString(s)
      case n: Number => n.toString
      case b: Boolean => b.toString
      case t: LocalDate => t.format(DateTimeFormatter.ISO_DATE)
      case t: LocalDateTime => t.format(DateTimeFormatter.ISO_DATE_TIME)
      case t: Timestamp => t.toLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
      case t: Date => t.toLocalDate.format(DateTimeFormatter.ISO_DATE)
      case j: JValue => formatString(JsonMethods.compact(j))
      case a: AnyRef => a.toString
    }

  def formatString(field: String): String =
    if (requiresQuotes(field)) {
      val dq = "\""
      dq + field.replace(dq, dq + dq) + dq
    } else {
      field
    }

  def requiresQuotes(field: String): Boolean =
    List("\"", "\\", ",", "\n").exists(field.contains)

  def snakecasify(value: String): String = {
    val replacements = Map(
      'ä' -> 'a',
      'ö' -> 'o',
      'å' -> 'a',
      ' ' -> '_',
    )
    val s = value
      .foldLeft(("", false)) { case ((out, prevUpper), char) =>
        val upper = char.isUpper
        (s"$out${if (upper && !prevUpper) "_" else ""}${char}", upper)
      }
      ._1
      .toLowerCase
    replacements.foldLeft(s) { case (in, (from, to)) => in.replace(from, to) }
  }
}
