package fi.oph.koski

import fi.oph.koski.koskiuser.{AuthenticationSupport, UserAuthenticationContext}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

class IndexServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before() {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
    indexHtml()
  }

  get("/uusioppija") {
    indexHtml()
  }

  get("/oppija/:oid") {
    indexHtml()
  }

  get("/omattiedot") {
    indexHtml()
  }

  get("/tiedonsiirrot*") {
    indexHtml()
  }

  private def indexHtml() = htmlIndex("koski-main.js")
}

class LoginPageServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }
}
