package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, MyDataSupport, OmaOpintopolkuSupport}
import org.scalatra.ScalatraServlet


class MyDataReactServlet(implicit val application: KoskiApplication) extends ScalatraServlet
  with HtmlServlet with AuthenticationSupport with OmaOpintopolkuSupport with MyDataSupport {

  before("/:memberCode") {
    setLangCookieFromDomainIfNecessary
    sessionOrStatus match {
      case Right(_) if shibbolethCookieFound =>
      case Left(_) if shibbolethCookieFound => redirect("/user/omadatalogin/hsl")
      case _ => redirect(getLoginUrlForMember(params.getAs[String]("memberCode").get))
    }
  }

  get("/:memberCode") {
      landerHtml
  }

  private def landerHtml = htmlIndex(
    scriptBundleName = "koski-omadata.js"
  )
}
