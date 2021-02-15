package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.KoskiSpecificBaseServlet

trait Unauthenticated extends KoskiSpecificBaseServlet {
  override def koskiSessionOption = None
}
