package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class SuoritusjakoHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/:secret") {
    htmlIndex("koski-suoritusjako.js", responsive = true)
  }
}
