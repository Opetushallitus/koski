package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import org.scalatra.ScalatraServlet

class PulssiHtmlServlet(val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/") {
    htmlIndex("koski-pulssi.js")
  }
}
