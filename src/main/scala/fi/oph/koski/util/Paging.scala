package fi.oph.koski.util

import fi.oph.koski.servlet.KoskiBaseServlet
import slick.lifted.Query

trait Paging extends KoskiBaseServlet {
  def pageNumber = getOptionalIntegerParam("pageNumber").getOrElse(0)
  def pageSize = getOptionalIntegerParam("pageSize").getOrElse(100)
  def pageInfo: PageInfo = PageInfo(pageNumber, pageSize)
}

case class PageInfo(page: Int, size: Int)

object QueryPaging {
  def paged[E, U, C[_]](query: Query[E, U, C], pageInfo: PageInfo):Query[E, U, C] = query.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)

  def paged[E, U, C[_]](query: Query[E, U, C], pageInfo: Option[PageInfo]): Query[E, U, C] = pageInfo match {
    case Some(pageInfo) => paged(query, pageInfo)
    case None => query
  }
}

object ListPaging {
  def paged[X](xs: Seq[X], pageInfo: PageInfo): Seq[X] = xs.drop(pageInfo.page * pageInfo.size).take(pageInfo.size)
  def paged[X](xs: Seq[X], pageInfo: Option[PageInfo]): Seq[X] = pageInfo match {
    case Some(pageInfo) => paged(xs, pageInfo)
    case None => xs
  }
}