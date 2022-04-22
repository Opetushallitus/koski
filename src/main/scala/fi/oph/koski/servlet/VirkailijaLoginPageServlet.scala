package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import org.scalatra.ScalatraServlet

class VirkailijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with KoskiSpecificSSOSupport {

  def allowFrameAncestors: Boolean = Environment.isLocalDevelopmentEnvironment(application.config)

  get("/")(nonce => {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js", nonce = nonce)
    }
  })
}
