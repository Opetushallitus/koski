package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import org.scalatra.ScalatraServlet

class SuoritusjakoHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/suoritetut-tutkinnot/:secret")(nonce => {
    setLangCookieFromDomainIfNecessary
    htmlIndex("koski-suoritetuttutkinnot.js", responsive = true, nonce = nonce)
  })

  get("/:secret")(nonce => {
    setLangCookieFromDomainIfNecessary
    htmlIndex("koski-suoritusjako.js", responsive = true, nonce = nonce)
  })
}
