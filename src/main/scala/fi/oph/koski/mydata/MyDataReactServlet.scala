package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, OmaOpintopolkuSupport}
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport with OmaOpintopolkuSupport {
  before("/:id") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect("/user/omadatalogin/hsl")
      case _ => redirect("/koski/login/shibboleth")
    }
  }

  get("/:id") {
      landerHtml
  }

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-omadata.js",
    scripts = <script id="auth">
      {Unparsed(s"""window.kansalaisenAuthUrl="$shibbolethUrl"""")}
    </script>,
    responsive = true
  )
}
