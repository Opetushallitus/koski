package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, MyDataSupport, OmaOpintopolkuSupport}
import org.scalatra.ScalatraServlet

import scala.util.matching.Regex


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with HtmlServlet with AuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  val nonErrorPage: Regex = "^(?!/error)\\S+$".r

  /*
    If user has not logged in, then:
    -redirect to Tupas, which
    -redirects to our Shibboleth page (based on 'login' parameter, or 'target' parameter in production), which
    -redirects back here (based on 'onSuccess' parameter).
   */

  before(nonErrorPage) {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect(getLoginURL(getCurrentURL))
      case _ => redirect(getShibbolethLoginURL(getCurrentURL, lang))
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
