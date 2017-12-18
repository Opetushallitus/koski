package fi.oph.koski.sso

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, KoskiSession}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class ShibbolethLoginServlet(application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache{
  get("/") {
    checkAuth.getOrElse(login)
  }

  private def login = {
    request.header("hetu") match {
      case Some(hetu) =>
        application.henkilöRepository.findOppijat(hetu)(KoskiSession.untrustedUser).headOption match {
          case Some(oppija) =>
            setUser(Right(localLogin(AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None, kansalainen = true))))
            redirect(s"$rootUrl/omattiedot")
          case _ =>
            haltWithStatus(KoskiErrorCategory.notFound("oppija not found"))
        }
      case _ => haltWithStatus(KoskiErrorCategory.badRequest("hetu header missing"))
    }
  }

  private def checkAuth: Option[HttpStatus] = request.header("security") match {
    case Some(password) if password == application.config.getString("shibboleth.security") => None
    case Some(_) => Some(KoskiErrorCategory.unauthorized())
    case None => Some(KoskiErrorCategory.badRequest("auth header missing"))
  }

  private def rootUrl =
    if (Environment.isLocalDevelopmentEnvironment) ""
    else application.config.getString("koski.root.url")
}