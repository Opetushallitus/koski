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
      case Left(_) if shibbolethCookieFound => redirect(getLoginSuccessTarget())
      case _ => redirect(getLoginUrlForMember(lang))
    }
  }

  before("/kayttooikeudet") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect(loginWithTarget("/koski/omadata/kayttooikeudet"))
      case _ => redirect(shibbolethLoginWithTarget("/koski/omadata/kayttooikeudet"))
    }
  }

  def loginWithTarget(target: String, encode: Boolean = false): String = {
    if (encode) {
      s"/koski/user/omadatalogin${URLEncoder.encode(s"?onLoginSuccess=${target}", "UTF-8")}"
    } else {
      s"/koski/user/omadatalogin?onLoginSuccess=${target}"
    }
  }

  def shibbolethLoginWithTarget(target: String) = {
    s"/koski/login/shibboleth?login=${loginWithTarget(target, encode = true)}"
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
