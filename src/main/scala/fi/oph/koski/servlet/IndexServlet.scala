package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OppijaRaamitSupport {
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
    def redirectUrl = koskiSessionOption.map { user =>
      if (user.user.kansalainen) "/omattiedot"
      else "/virkailija"
    }.getOrElse("/virkailija")

    if (application.features.shibboleth && !isAuthenticated) {
      landerHtml
    } else {
      redirect(redirectUrl)
    }
  }

  get("/virkailija") {
    indexHtml
  }

  get("/validointi") {
    indexHtml
  }

  get("/uusioppija") {
    indexHtml
  }

  get("/oppija/:oid") {
    indexHtml
  }

  get("/omattiedot") {
    htmlIndex(
      scriptBundleName = "koski-omattiedot.js",
      raamit = oppijaRaamit,
      responsive = true
    )
  }

  get("/tiedonsiirrot*") {
    indexHtml
  }

  private def indexHtml =
    htmlIndex("koski-main.js", raamit = virkailijaRaamit)

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-lander.js",
    raamit = oppijaRaamit,
    scripts = <script id="auth">
      {Unparsed(s"""window.kansalaisenAuthUrl="$shibbolethUrl"""")}
    </script>,
    responsive = true
  )
}

