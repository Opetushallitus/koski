package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

import scala.xml.NodeSeq.Empty
import scala.xml.Unparsed

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
    val (bundle, scripts) = if (application.features.shibboleth) {
      ("koski-lander.js", <script id="auth">{Unparsed(s"""window.kansalaisenAuthUrl="${application.config.getString("shibboleth.url")}"""")}</script>)
    }  else {
      ("koski-landerWithLogin.js", Empty)
    }
    htmlIndex(bundle, scripts = scripts)
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
}
