package fi.oph.koski.omattiedot

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet

class OmatTiedotHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  before("/") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) =>
      case Left(_) => {
        redirectToOppijaLogin
      }
    }
  }

  get("/")(nonce =>{
    htmlIndex(
      scriptBundleName = "koski-omattiedot.js",
      raamit = oppijaRaamit,
      responsive = true,
      nonce = nonce
    )
  })
}
