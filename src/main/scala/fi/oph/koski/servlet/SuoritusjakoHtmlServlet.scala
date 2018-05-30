package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class SuoritusjakoHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OmaOpintopolkuSupport {
  get("/:secret") {
    setLangCookieFromDomainIfNecessary
    htmlIndex("koski-suoritusjako.js", raamit = oppijaRaamit, responsive = true)
  }
}
