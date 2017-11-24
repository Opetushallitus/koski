package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.servlet.{HtmlServlet, NoCache}

case class ShibbolethLoginServlet(application: KoskiApplication) extends HtmlServlet with AuthenticationSupport with NoCache{
  get("/") {
    request.header("nationalidentificationnumber") match {
      case Some(hetu) => <html>SUCCESS</html>
      case None => haltWithStatus(KoskiErrorCategory.badRequest("nationalidentificationnumber header missing"))
    }
  }
}