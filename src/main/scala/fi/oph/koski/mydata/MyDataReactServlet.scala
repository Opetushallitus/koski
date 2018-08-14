package fi.oph.koski.mydata

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, MyDataSupport, OmaOpintopolkuSupport}
import org.scalatra.ScalatraServlet


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with HtmlServlet with AuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  /*
    If user has not logged in, then:
    -redirect to Tupas, which
    -redirects to our Shibboleth page (based on 'login' parameter, or 'target' parameter in production), which
    -redirects back here (based on 'onLoginSuccess' parameter).
   */
  before("/valtuutus/:memberCode") {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect(getLoginURL(getCurrentURL))
      case _ => redirect(getShibbolethLoginURL(getCurrentURL, lang))
    }
  }

  before("/kayttooikeudet") {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect(getLoginURL("/koski/omadata/kayttooikeudet"))
      case _ => redirect(getShibbolethLoginURL("/koski/omadata/kayttooikeudet", lang))
    }
  }

  get("/valtuutus/:memberCode") {
      landerHtml
  }

  get("/kayttooikeudet") {
    htmlIndex(
      scriptBundleName = "koski-kayttooikeudet.js",
      raamit = oppijaRaamit,
      responsive = true
    )
  }

  get("/error/:message") {
    status = 404
    landerHtml
  }

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-omadata.js",
    responsive = true
  )
}
