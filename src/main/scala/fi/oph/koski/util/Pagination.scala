package fi.oph.koski.util

import fi.oph.koski.servlet.KoskiBaseServlet
import slick.lifted.Query
import scala.language.higherKinds

trait Pagination extends KoskiBaseServlet {
  def pageNumber = getOptionalIntegerParam("pageNumber")
  def pageSize = getOptionalIntegerParam("pageSize").map(ps => Math.min(ps, 10000)) // Limit to 10000 items per page
  def paginationSettings: Option[PaginationSettings] = (pageNumber, pageSize) match {
    case (pageNumber, Some(pageSize)) => Some(PaginationSettings(pageNumber.getOrElse(0), pageSize))
    case _ => None
  }
}

case class PaginationSettings(page: Int, size: Int)

object QueryPagination {
  def applyPagination[E, U, C[_]](query: Query[E, U, C], pageInfo: PaginationSettings):Query[E, U, C] = query.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)

  def applyPagination[E, U, C[_]](query: Query[E, U, C], pageInfo: Option[PaginationSettings]): Query[E, U, C] = pageInfo match {
    case Some(pageInfo) => applyPagination(query, pageInfo)
    case None => query
  }
}

object ListPagination {
  def applyPagination[X](pageInfo: PaginationSettings, xs: Seq[X]): Seq[X] = xs.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)

  def applyPagination[X](pageInfo: Option[PaginationSettings], xs: Seq[X]): Seq[X] = pageInfo match {
    case Some(pageInfo) => applyPagination(pageInfo, xs)
    case None => xs
  }
}

case class PaginatedResponse[T](result: T, paginationSettings: Option[PaginationSettings], mayHaveMore: Boolean)

object PaginatedResponse {
  def apply[T](paginationSetting: Option[PaginationSettings], result: T, resultSize: Int): PaginatedResponse[T] = paginationSetting match {
    case Some(s) => PaginatedResponse(result, paginationSetting, resultSize == s.size)
    case None => PaginatedResponse(result, None, false)
  }
}