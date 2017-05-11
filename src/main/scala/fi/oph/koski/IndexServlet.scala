package fi.oph.koski

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, UserAuthenticationContext}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport
import fi.oph.koski.util.XML
import org.scalatra.ScalatraServlet

import scala.xml.Elem

class IndexServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with AuthenticationSupport {
  before() {
    if (!isAuthenticated) {
      redirectToLogin
    }
  }

  get("/*") {
    val httpStatus = KoskiErrorCategory.notFound()
    status = httpStatus.statusCode
    XML.transform(indexHtml(Some(status))) {
      case e: Elem if e.label == "head" =>
        e copy (child = (e.child :+ htmlErrorObjectScript(httpStatus)) ++ piwikTrackErrorObject)
    }
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

  private def indexHtml(piwikHttpStatusCode: Option[Int] = None) =
    htmlIndex("koski-main.js", piwikHttpStatusCode = piwikHttpStatusCode)
}

class LoginPageServlet(val application: UserAuthenticationContext) extends ScalatraServlet with HtmlServlet with SSOSupport {
  get("/") {
    if (ssoConfig.isCasSsoUsed) {
      redirect("/")
    } else {
      htmlIndex("koski-login.js")
    }
  }
}
