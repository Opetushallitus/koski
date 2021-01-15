package fi.oph.koski.koskiuser

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.{VirkailijaHtmlServlet}
import fi.oph.koski.sso.SSOSupport

class LogoutServlet(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet with SSOSupport {
  get("/") {
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
        case Some(target) => luvanluovutusLogout(target)
        case None => redirectToOppijaLogout
      }
    }
  }

  private def luvanluovutusLogout(target: String) = {
    val url = casOppijaLogoutUrl(encode(target))
    redirect(url)
  }

  private def encode(param: String) = URLEncoder.encode(param, "UTF-8")
}

object LogoutServerConfiguration {
  var overrides: Map[String, String] = Map.empty

  def shibbolethLogoutUrl(application: KoskiApplication, lang: String) = {
    val key = "logout.url." + lang
    overrides.get(key).getOrElse(application.config.getString(key))
  }

  def configurableShibbolethLogoutUrl(application: KoskiApplication, lang: String) = {
    val key = "configurable.logout.url." + lang
    overrides.get(key).getOrElse(application.config.getString(key))
  }

  def overrideKey(key: String, value: String): Unit = {
    overrides = overrides + (key -> value)
  }

  def clearOverrides = {
    overrides = Map.empty
  }
}
