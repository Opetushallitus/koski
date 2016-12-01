package fi.oph.koski.util

import fi.oph.koski.servlet.KoskiBaseServlet
import slick.lifted.Query

trait Pagination extends KoskiBaseServlet {
  def pageNumber = getOptionalIntegerParam("pageNumber").getOrElse(0)
  def pageSize = getOptionalIntegerParam("pageSize").getOrElse(100)
  def paginationSettings: PaginationSettings = PaginationSettings(pageNumber, pageSize)
}

case class PaginationSettings(page: Int, size: Int)

object QueryPagination {
  def paged[E, U, C[_]](query: Query[E, U, C], pageInfo: PaginationSettings):Query[E, U, C] = query.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)

  def paged[E, U, C[_]](query: Query[E, U, C], pageInfo: Option[PaginationSettings]): Query[E, U, C] = pageInfo match {
    case Some(pageInfo) => paged(query, pageInfo)
    case None => query
  }
}

object ListPagination {
  def paged[X](pageInfo: PaginationSettings, xs: Seq[X]): Seq[X] = xs.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)

  def paged[X](pageInfo: Option[PaginationSettings], xs: Seq[X]): Seq[X] = pageInfo match {
    case Some(pageInfo) => paged(pageInfo, xs)
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