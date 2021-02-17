package fi.oph.koski.valpas.servlet

import fi.oph.koski.servlet.BaseServlet
import fi.oph.koski.valpas.valpasuser.ValpasSession

trait ValpasBaseServlet extends BaseServlet {
  def koskiSessionOption: Option[ValpasSession]
}
