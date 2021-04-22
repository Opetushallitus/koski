package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Raamit}

trait OppijaHtmlServlet extends KoskiHtmlServlet {
  def virkailijaRaamitSet: Boolean = false
  def virkailijaRaamit: Raamit = EiRaameja
}
