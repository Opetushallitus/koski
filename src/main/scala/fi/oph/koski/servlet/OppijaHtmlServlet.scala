package fi.oph.koski.servlet

import fi.oph.koski.html.{EiRaameja, Raamit}

trait OppijaHtmlServlet extends KoskiHtmlServlet {
  protected val virkailijaRaamitSet: Boolean = false

  protected def virkailijaRaamit: Raamit = EiRaameja
}
