package fi.oph.koski.valpas.valpasuser

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.SessionStatusExpiredVirkailija
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet

class ValpasLogoutServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with ValpasCookieAndBasicAuthAuthenticationSupport {
  get("/") {
    logger.info("Logged out")

    val virkailija = sessionOrStatus match {
      case Right(session) if !session.user.kansalainen => true
      case Left(SessionStatusExpiredVirkailija) => true
      case _ => false
    }

    getUser.toOption.flatMap(_.serviceTicket).foreach(application.koskiSessionRepository.removeSessionByTicket)
    removeUserCookie

    if (virkailija) {
      redirectToVirkailijaLogout
    } else {
      params.get("target") match {
        case Some(target) if target != "/" => {
          redirectToOppijaLogout(target)
        }
        case _ => redirectToOppijaLogout(serviceRoot)
      }
    }
  }

  private def encode(param: String) = URLEncoder.encode(param, "UTF-8")
}
