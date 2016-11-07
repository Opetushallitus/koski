package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.KoskiBaseServlet

trait Unauthenticated extends KoskiBaseServlet {
  override def koskiSessionOption = None
}
