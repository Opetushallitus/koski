package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiAuthenticationSupport
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet

import scala.util.matching.Regex


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with OppijaHtmlServlet with KoskiAuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  val nonErrorPage: Regex = "^(?!/error)\\S+$".r

  /*
    If user has not logged in, then:
    -redirect to Tupas, which
    -redirects to our cas page (based on 'login' parameter, or 'target' parameter in production), which
    -redirects back here (based on 'onSuccess' parameter).
   */

  before(nonErrorPage) {
    setLangCookieFromDomainIfNecessary
    val lang = langFromCookie.getOrElse(langFromDomain)

    sessionOrStatus match {
      case Right(_) =>
      case Left(_) => redirect(getCasLoginURL(getCurrentURL, lang))
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
