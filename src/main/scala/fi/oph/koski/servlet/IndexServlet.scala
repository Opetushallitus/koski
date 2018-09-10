package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OmaOpintopolkuSupport {
  before("/omattiedot") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect("/user/shibbolethlogin")
      case _ => redirect(shibbolethUrl)
    }
  }

  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/") {
    if (application.features.shibboleth && !isAuthenticated) {
      setLangCookieFromDomainIfNecessary
      landerHtml
    } else {
      val url = if (koskiSessionOption.exists(_.user.kansalainen)) {
        "/omattiedot"
      } else {
        "/virkailija"
      }
      redirect(url)
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

  get("/raportit*") {
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

