package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

class VirkailijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }
}
