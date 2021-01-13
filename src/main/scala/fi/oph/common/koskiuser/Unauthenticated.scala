package fi.oph.common.koskiuser

import fi.oph.koski.servlet.KoskiBaseServlet

trait Unauthenticated extends KoskiBaseServlet {
  override def koskiSessionOption = None
}
