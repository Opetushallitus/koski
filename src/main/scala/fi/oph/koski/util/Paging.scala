package fi.oph.koski.util

import fi.oph.koski.servlet.KoskiBaseServlet

trait Paging extends KoskiBaseServlet {
  def pageNumber = getOptionalIntegerParam("pageNumber").getOrElse(0)
  def pageSize = getOptionalIntegerParam("pageSize").getOrElse(1000)
  def pageInfo = PageInfo(pageNumber, pageSize)
}

case class PageInfo(page: Int, size: Int)