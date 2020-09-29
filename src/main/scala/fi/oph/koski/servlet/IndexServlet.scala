package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed



class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with OmaOpintopolkuSupport {
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
    if (koskiSessionOption.exists(_.hasKelaAccess)) {
      redirect("/kela")
    } else {
      indexHtml
    }
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

  get("/tiedonsiirrot*") {
    indexHtml
  }

  get("/raportit*") {
    indexHtml
  }

  get("/kela*") {
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

