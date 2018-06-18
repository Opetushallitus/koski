package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, MyDataSupport, OmaOpintopolkuSupport}
import org.scalatra.ScalatraServlet


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with HtmlServlet with AuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  /*
    If user has not logger in, then:
    -redirect to Tupas, which
    -redirects to our Shibboleth page (based on 'login' parameter, or 'target' parameter in production), which
    -redirects back here (based on 'onLoginSuccess' parameter).
   */
  before("/:memberCode") {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect(getLoginSuccessTarget(params("memberCode"), lang))
      case _ => redirect(getLoginUrlForMember(params("memberCode"), lang))
    }
  }

  get("/:memberCode") {
      landerHtml
  }

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-omadata.js",
    responsive = true
  )
}
