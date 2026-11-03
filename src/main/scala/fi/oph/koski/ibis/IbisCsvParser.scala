package fi.oph.koski.ibis

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

case class IbisParseError(line: Int, message: String)

object IbisCsvParser {
  private val fixedFieldCount = 15
  private val subjectFieldCount = 7
  private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  // IBIS-tiedostossa ei dokumentaation mukaan ole otsikkoriviä, mutta se ohitetaan, jos sellainen on.
  def parse(content: String): (List[IbisParseError], List[IbisRecord]) = {
    val lines = content.split("\r?\n").toList.zipWithIndex
      .map { case (line, index) => (line, index + 1) }
      .filter { case (line, _) => line.trim.nonEmpty }
    val dataLines = lines match {
      case (first, _) :: rest if isHeader(first) => rest
      case all => all
    }
    val parsed = dataLines.map { case (line, lineNumber) => parseLine(splitLine(line), lineNumber) }
    (parsed.collect { case Left(error) => error }, parsed.collect { case Right(record) => record })
  }

  private def isHeader(line: String): Boolean =
    splitLine(line).headOption.exists(_.trim.equalsIgnoreCase("YEAR"))

  def parseLine(rawFields: List[String], lineNumber: Int): Either[IbisParseError, IbisRecord] = {
    val fields = rawFields.map(_.trim)
    val subjectFields = fields.drop(fixedFieldCount)
    if (fields.length < fixedFieldCount) {
      Left(IbisParseError(lineNumber, s"Rivillä on ${fields.length} kenttää, odotettiin vähintään $fixedFieldCount"))
    } else if (subjectFields.length % subjectFieldCount != 0) {
      Left(IbisParseError(lineNumber, s"Oppiainekenttien määrä ${subjectFields.length} ei ole jaollinen luvulla $subjectFieldCount"))
    } else {
      Try(toRecord(fields, subjectFields)).toEither.left.map(e => IbisParseError(lineNumber, s"Rivin jäsennys epäonnistui: ${e.getMessage}"))
    }
  }

  private def toRecord(fields: List[String], subjectFields: List[String]): IbisRecord = {
    val subjects = subjectFields.grouped(subjectFieldCount).toList
      .filter(_.exists(_.nonEmpty))
      .map {
        case List(year, month, code, level, grade, additional, language) =>
          IbisSubject(year.toInt, validMonth(month), code, level.toUpperCase, grade.toUpperCase, additional.equalsIgnoreCase("Y"), language)
        case group =>
          throw new IllegalArgumentException(s"Virheellinen oppiainekenttien ryhmä: $group")
      }
    IbisRecord(
      year = fields(0).toInt,
      month = validMonth(fields(1)),
      school = fields(2),
      personalCode = fields(3),
      dateOfBirth = parseDate(fields(4)),
      gender = fields(5).toUpperCase,
      category = fields(6).toUpperCase,
      lastName = fields(7),
      firstName = fields(8),
      studentId = fields(9),
      courseRef = fields(10),
      lastChangeDate = parseDate(fields(11)),
      totalPoints = parseInt(fields(12)),
      resultCode = fields(13).toUpperCase,
      eeTokPoints = parseInt(fields(14)),
      subjects = subjects,
    )
  }

  private def validMonth(month: String): String = {
    val normalized = month.toUpperCase
    if (IbisSession.validMonth(normalized)) normalized else throw new IllegalArgumentException(s"Tuntematon tutkintokerran kuukausi $month")
  }

  // EE/TOK POINTS on muotoa "+2"
  private def parseInt(s: String): Option[Int] = Some(s.stripPrefix("+")).filter(_.nonEmpty).map(_.toInt)

  private def parseDate(s: String): Option[LocalDate] = Some(s).filter(_.nonEmpty).map(LocalDate.parse(_, dateFormat))

  // RFC 4180 -tyylinen jako: kentät voivat olla lainausmerkeissä, jolloin kentän sisäinen lainausmerkki on kahdennettu.
  def splitLine(line: String): List[String] = {
    val end = line.foldLeft(SplitState()) { (state, c) =>
      if (state.quotePending) {
        c match {
          case '"' => state.copy(current = state.current + c, quotePending = false)
          case ',' => state.copy(fields = state.fields :+ state.current, current = "", inQuotes = false, quotePending = false)
          case _ => state.copy(current = state.current + c, inQuotes = false, quotePending = false)
        }
      } else if (state.inQuotes) {
        c match {
          case '"' => state.copy(quotePending = true)
          case _ => state.copy(current = state.current + c)
        }
      } else {
        c match {
          case '"' => state.copy(inQuotes = true)
          case ',' => state.copy(fields = state.fields :+ state.current, current = "")
          case _ => state.copy(current = state.current + c)
        }
      }
    }
    (end.fields :+ end.current).toList
  }

  private case class SplitState(
    fields: Vector[String] = Vector.empty,
    current: String = "",
    inQuotes: Boolean = false,
    quotePending: Boolean = false,
  )
}
