package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class SuoritusjakoHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {
  get("/:secret")(nonce => {
    setLangCookieFromDomainIfNecessary
    htmlIndex("koski-suoritusjako.js", responsive = true, nonce = nonce)
  })
}
