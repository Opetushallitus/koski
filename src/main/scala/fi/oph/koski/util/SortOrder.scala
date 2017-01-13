package fi.oph.koski.util

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.InvalidRequestException

trait SortOrder {
  def field: String
  def descending: Boolean
}

object SortOrder {
  case class Ascending(field: String) extends SortOrder {
    def descending = false
  }
  case class Descending(field: String) extends SortOrder {
    def descending = true
  }

  def parseSortOrder(orderString: Option[String], defaultSort: SortOrder) = orderString.map {
    str => str.split(":") match {
      case Array(key: String, "asc") => Ascending(key)
      case Array(key: String, "desc") => Descending(key)
      case xs => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Invalid sort param. Expected key:asc or key: desc"))
    }
  }.getOrElse(defaultSort)
}