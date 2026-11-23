package fi.oph.koski.ibis

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

case class IbisParseError(line: Int, message: String)

object IbisCsvParser {
  private val fixedFieldCount = 15
  private val subjectFieldCount = 7
  private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  // Tiedosto ei välttämättä ole UTF-8:aa. Virheelliset tavut korvataan, koska tuonnin käyttämät
  // kentät ovat ASCII-merkkejä.
  def parse(bytes: Array[Byte]): (List[IbisParseError], List[IbisRecord]) =
    parse(new String(bytes, StandardCharsets.UTF_8))

  // IBIS-tiedostossa ei dokumentaation mukaan ole otsikkoriviä, mutta se ohitetaan, jos sellainen on.
  def parse(content: String): (List[IbisParseError], List[IbisRecord]) = {
    val lines = content.stripPrefix("\uFEFF").split("\r?\n").toList.zipWithIndex
      .collect { case (line, index) if line.trim.nonEmpty => (rivinKentät(line), index + 1) }
    val dataLines = lines match {
      case (first, _) :: rest if isHeader(first) => rest
      case all => all
    }
    val parsed = dataLines.map { case (fields, lineNumber) => parseLine(fields, lineNumber) }
    (parsed.collect { case Left(error) => error }, parsed.collect { case Right(record) => record })
  }

  // Kun tiedosto avataan ja tallennetaan Excelissä, koko rivi voi päätyä yhdeksi lainausmerkeissä olevaksi kentäksi.
  private def rivinKentät(line: String): List[String] = splitLine(line) match {
    case List(koko) if koko.contains(',') => splitLine(koko)
    case kentät => kentät
  }

  private def isHeader(fields: List[String]): Boolean =
    fields.headOption.exists(_.trim.equalsIgnoreCase("YEAR"))

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
        case List(year, month, name, level, grade, additional, language) =>
          IbisSubject(year.toInt, month.toUpperCase, name, level.toUpperCase, grade.toUpperCase, additional.equalsIgnoreCase("Y"), language)
        case group =>
          throw new IllegalArgumentException(s"Virheellinen oppiainekenttien ryhmä: $group")
      }
    IbisRecord(
      year = fields(0).toInt,
      month = fields(1).toUpperCase,
      school = fields(2),
      iboTunniste = fields(3),
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
      combinedGrade = parseInt(fields(14)),
      subjects = subjects,
    )
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
