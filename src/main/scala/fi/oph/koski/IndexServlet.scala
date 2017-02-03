package fi.oph.koski

import java.io.File
import java.util.Properties

import fi.oph.koski.koskiuser.{AuthenticationSupport, UserAuthenticationContext}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import org.scalatra.ScalatraServlet

class IndexServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before() {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/*") {
    status = 404
    indexHtml()
  }

  get("/") {
    indexHtml()
  }

  get("/uusioppija") {
    indexHtml()
  }

  get("/oppija/:oid") {
    indexHtml()
  }

  get("/omattiedot") {
    indexHtml()
  }

  get("/tiedonsiirrot*") {
    indexHtml()
  }

  def indexHtml() = IndexServlet.html(buildversion = buildversion)
}

object IndexServlet {
  def html(scriptBundleName: String = "koski-main.js", buildversion: Option[String]) =
    <html>
      <head>
        <title>Koski - Opintopolku.fi</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta charset="UTF-8" />
        <link rel="shortcut icon" href="/koski/favicon.ico" />
        <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.min.css" />
        <link href="//fonts.googleapis.com/css?family=Open+Sans:400,600,700" rel="stylesheet" type="text/css" />
        <link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
      </head>
      <body>
        <div id="content"></div>
      </body>
      <script id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildversion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
    </html>

  def scriptTimestamp(scriptBundleName: String) = new File(s"./target/webapp/js/${scriptBundleName}").lastModified()
}

class LoginPageServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      IndexServlet.html("koski-login.js", buildversion = buildversion)
    }
  }
}
