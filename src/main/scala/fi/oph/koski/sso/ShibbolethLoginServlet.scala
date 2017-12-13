package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser, KoskiSession}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class ShibbolethLoginServlet(application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache{
  get("/") {
    request.header("nationalidentificationnumber") match {
      case Some(hetu) =>
        application.henkilöRepository.findOppijat(hetu)(KoskiSession.untrustedUser).headOption match {
          case Some(oppija) =>
            setUser(Right(localLogin(AuthenticationUser(oppija.oid, oppija.oid, s"${oppija.etunimet} ${oppija.sukunimi}", None))), kansalainen = true)
            redirect(s"${application.config.getString("koski.root.url")}/omattiedot")
          case _ =>
            haltWithStatus(KoskiErrorCategory.notFound("oppija not found"))
        }
      case _ => haltWithStatus(KoskiErrorCategory.badRequest("nationalidentificationnumber header missing"))
    }
  }
}