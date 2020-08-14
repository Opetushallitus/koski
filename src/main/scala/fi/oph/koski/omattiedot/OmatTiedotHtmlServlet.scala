package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.{OmaOpintopolkuSupport, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet

class OmatTiedotHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {
  before("/") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect("/user/shibbolethlogin")
      case _ => redirect(shibbolethUrl)
    }
  }

  get("/") {
    htmlIndex(
      scriptBundleName = "koski-omattiedot.js",
      raamit = oppijaRaamit,
      responsive = true
    )
  }
}
