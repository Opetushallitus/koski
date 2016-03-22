package fi.oph.tor.date

import java.util.Date
import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.servlet.InvalidRequestException
import org.joda.time.DateTime

object ISO8601DateParser {
  def parseDateTime(dateTime: String): Date = {
    parse(dateTime).toDate
  }

  private def parse(date: String): DateTime = {
    try {
      new DateTime(date);
    } catch {
      case e: IllegalArgumentException => throw new InvalidRequestException(TorErrorCategory.badRequest.format.pvm, "wrong date format " + date + ", expected ISO8601")
    }
  }
}
