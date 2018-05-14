package fi.oph.koski

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.html.{EiRaameja, Oppija, Virkailija}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, SessionStatusExpiredKansalainen}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

import scala.xml.Unparsed

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
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
      raamit = if (raamitHeaderSet) oppijaRaamit else EiRaameja,
      responsive = true
    )
  }

  get("/tiedonsiirrot*") {
    indexHtml
  }

  private def indexHtml =
    htmlIndex("koski-main.js", raamit = if (raamitHeaderSet) Virkailija else EiRaameja)

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-lander.js",
    scripts = <script id="auth">
      {Unparsed(s"""window.kansalaisenAuthUrl="${application.config.getString("shibboleth.url." + lang)}"""")}
    </script>,
    responsive = true
  )

  private def oppijaRaamit = Oppija(koskiSessionOption, application.config.getString("shibboleth.url"))
}

class EiSuorituksiaServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/") {
    htmlIndex("koski-eisuorituksia.js", responsive = true)
  }
}

class SuoritusjakoHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/:secret") {
    htmlIndex("koski-suoritusjako.js", raamit = EiRaameja, responsive = true)
  }
}

class VirhesivuServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  get("/") {
    response.setHeader("X-Virhesivu", "1") // for korhopankki/HetuLogin.jsx
    <html>
      <head>
        <title>Koski - Virhe</title>
        <link type="text/css" rel="stylesheet" href="/koski/css/virhesivu.css"/>
      </head>
      <body>
        <div class="odottamaton-virhe">
          <h2>Koski-järjestelmässä tapahtui virhe, yritä myöhemmin uudelleen</h2>
          <a href="/koski/">Palaa etusivulle</a>
        </div>
      </body>
    </html>
  }
}

class LoginPageServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }

  get("/shibboleth") {
    if (application.features.shibboleth && application.config.getString("shibboleth.security") == "mock") {
      htmlIndex("koski-korhopankki.js", responsive = true)
    } else {
      logger.error("Mock shibboleth in use, please check shibboleth.url config")
      haltWithStatus(KoskiErrorCategory.notFound())
    }
  }
}
