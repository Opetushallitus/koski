package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import org.scalatra.ScalatraServlet

class EiSuorituksiaServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {

  def allowFrameAncestors: Boolean = Environment.isLocalDevelopmentEnvironment(application.config)

  get("/")(nonce => {
    htmlIndex("koski-eisuorituksia.js", raamit = oppijaRaamit, responsive = true, nonce = nonce)
  })
}
