package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class EiSuorituksiaServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OppijaRaamitSupport {
  get("/") {
    htmlIndex("koski-eisuorituksia.js", raamit = oppijaRaamit, responsive = true)
  }
}
