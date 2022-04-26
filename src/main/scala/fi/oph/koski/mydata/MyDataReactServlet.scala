package fi.oph.koski.mydata

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet

import scala.util.matching.Regex


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with OppijaHtmlServlet with KoskiSpecificAuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))


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

  get("/valtuutus/:memberCode")(landerHtml)

  get("/kayttooikeudet")(nonce => {
    htmlIndex(
      scriptBundleName = "koski-kayttooikeudet.js",
      raamit = oppijaRaamit,
      responsive = true,
      nonce = nonce
    )
  })

  get("/error/:message")(nonce => {
    status = 404
    landerHtml(nonce)
  })

  private def landerHtml(nonce: String) = htmlIndex(
    scriptBundleName = "koski-omadata.js",
    responsive = true,
    nonce = nonce
  )
}
