package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.sso.KoskiSSOSupport
import org.scalatra.ScalatraServlet

class VirkailijaLoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with KoskiSSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }
}
