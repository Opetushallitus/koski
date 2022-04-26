package fi.oph.koski.koskiuser

import java.net.URLEncoder
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.servlet.VirkailijaHtmlServlet
import fi.oph.koski.sso.KoskiSpecificSSOSupport

class KoskiSpecificLogoutServlet(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with KoskiSpecificSSOSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/")(nonce => {
    logger.info("Logged out")

    val virkailija = sessionOrStatus match {
      case Right(session) if !session.user.kansalainen => true
      case Left(SessionStatusExpiredVirkailija) => true
      case _ => false
    }

    getUser.right.toOption.flatMap(_.serviceTicket).foreach(application.koskiSessionRepository.removeSessionByTicket)
    removeUserCookie

    if (virkailija) {
      redirectToVirkailijaLogout
    } else {
      params.get("target") match {
        case Some(target) if target != "/" => {
          redirectToOppijaLogout(target)
        }
        case _ => redirectToOppijaLogout(serviceRoot + "/koski")
      }
    }
  })

  private def encode(param: String) = URLEncoder.encode(param, "UTF-8")
}

object LogoutServerConfiguration {
  var overrides: Map[String, String] = Map.empty

  def logoutUrl(application: KoskiApplication, lang: String) = {
    val key = "logout.url." + lang
    overrides.get(key).getOrElse(application.config.getString(key))
  }

  def overrideKey(key: String, value: String): Unit = {
    overrides = overrides + (key -> value)
  }

  def clearOverrides = {
    overrides = Map.empty
  }
}
