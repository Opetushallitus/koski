package fi.oph.koski.koskiuser

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport

class LogoutServlet(implicit val application: KoskiApplication) extends HtmlServlet with SSOSupport {
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
      redirectToLogout
    } else {
      redirect(getLogoutUrl)
    }
  }

  private[koskiuser] def getLogoutUrl: String = {
    if (request.parameters.contains("target")) {
      if (!application.config.getString("configurable.logout.url." + langFromDomain).isEmpty) {
        // We get redirected 3 times: first to shibboleth logout url, then to our own redirect url, from where we finally
        // redirect to the actual "target". This is why we url encode the parameter twice.
        application.config.getString("configurable.logout.url." + langFromDomain) + encode(encode(params("target")))
      } else {
        // redirect via our servlet so as not to allow "open / unvalidated redirects", this is used when there is no shibbo present
        "/user/redirect?target=" + encode(params("target"))
      }
    } else if (!application.config.getString("logout.url." + langFromDomain).isEmpty) {
      application.config.getString("logout.url." + langFromDomain)
    } else {
      "/"
    }
  }

  private def encode(param: String) = URLEncoder.encode(param, "UTF-8")
}
