package fi.oph.koski.date

import java.util.Date
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.InvalidRequestException
import org.joda.time.DateTime

object ISO8601DateParser {
  def parseDateTime(dateTime: String): Date = {
    parse(dateTime).toDate
  }

  private def parse(date: String): DateTime = {
    try {
      new DateTime(date);
    } catch {
      case e: IllegalArgumentException => throw new InvalidRequestException(KoskiErrorCategory.badRequest.format.pvm, "wrong date format " + date + ", expected ISO8601")
    }
  }
}
