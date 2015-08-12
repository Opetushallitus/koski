package fi.oph.tor.date

import java.text.SimpleDateFormat
import java.util.Date
import fi.oph.tor.InvalidRequestException

object FinnishDateParser {
  def parseDateTime(dateTime: String) = {
    parseWithFormat(dateTime, "d.M.yyyy HH:mm")
  }

  def parseDate(date: String) = {
    parseWithFormat(date, "d.M.yyyy")
  }

  def parseWithFormat(date: String, format: String): Date = {
    try {
      new SimpleDateFormat(format).parse(date)
    } catch {
      case e: java.text.ParseException => throw new InvalidRequestException("wrong data format " + date + ", expected " + format)
    }
  }
}
