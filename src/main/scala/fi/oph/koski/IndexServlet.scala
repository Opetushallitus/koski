package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

import scala.util.Try

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
    if (application.env.isDevEnvironment) {
      htmlIndex("koski-landerWithLogin.js")
    } else {
      htmlIndex("koski-lander.js")
    }
  }

  get("/virkailija") {
    indexHtml()
  }

  get("/validointi") {
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

  private def indexHtml() = {
    htmlIndex("koski-main.js", raamitEnabled = raamitHeaderSet)
  }
}

class LoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }
}
