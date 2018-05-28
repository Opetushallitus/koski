package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before("/omattiedot") {
    sessionOrStatus match {
      case Left(_) => redirectToFrontpage
      case Right(_) =>
    }
  }

  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
      landerHtml
  }

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-omadata.js",
    scripts = <script id="auth">
      {Unparsed(s"""window.kansalaisenAuthUrl="${application.config.getString("shibboleth.url")}"""")}
    </script>,
    responsive = true
  )
}
