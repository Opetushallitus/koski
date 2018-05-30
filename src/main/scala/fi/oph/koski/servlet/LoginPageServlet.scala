package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

class LoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }

  get("/shibboleth") {
    if (application.features.shibboleth && application.config.getString("shibboleth.security") == "mock") {
      htmlIndex("koski-korhopankki.js", responsive = true)
    } else {
      logger.error("Mock shibboleth in use, please check shibboleth.url config")
      haltWithStatus(KoskiErrorCategory.notFound())
    }
  }
}
