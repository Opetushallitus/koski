package fi.oph.tor.date

import java.util.Date

import fi.oph.tor.InvalidRequestException
import org.joda.time.DateTime

object ISO8601DateParser {
  def parseDateTime(dateTime: String): Date = {
    println(dateTime + " -> " + parse(dateTime).toDate)
    parse(dateTime).toDate
  }

  private def parse(date: String): DateTime = {
    try {
      new DateTime(date);
    } catch {
      case e: IllegalArgumentException => throw new InvalidRequestException("wrong date format " + date + ", expected ISO8601")
    }
  }
}
