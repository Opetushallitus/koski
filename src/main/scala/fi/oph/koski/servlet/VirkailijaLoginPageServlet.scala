package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import org.scalatra.ScalatraServlet

class VirkailijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with KoskiSpecificSSOSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/")(nonce => {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js", nonce = nonce)
    }
  })
}
