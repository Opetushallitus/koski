package fi.oph.koski

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
    htmlIndex("koski-lander.js", scripts =
      <script id="auth">{Unparsed(s"""window.kansalaisenAuthUrl="${application.config.getString("shibboleth.url")}"""")}</script>
    )
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
    indexHtml(disableRaamit = true)
  }

  get("/tiedonsiirrot*") {
    indexHtml()
  }

  private def indexHtml(disableRaamit: Boolean = false) = {
    htmlIndex("koski-main.js", raamitEnabled = !disableRaamit && raamitHeaderSet)
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

  get("/shibboleth") {
    if (Environment.isLocalDevelopmentEnvironment) {
      htmlIndex("koski-shibbolethLogin.js")
    } else {
      redirect("/")
    }
  }
}
