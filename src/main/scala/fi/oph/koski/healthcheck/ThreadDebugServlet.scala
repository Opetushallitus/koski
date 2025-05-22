package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{Http, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class ThreadDebugServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {

  def session: KoskiSpecificSession = koskiSessionOption.get

  get("/") {
    requireVirkailijaOrPalvelukäyttäjä
    if (session.hasRole(OPHPAAKAYTTAJA)) {
      val status = application.healthCheck.logStackTracesIfPoolGettingFull(-1)
      renderStatus(status)
    } else {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Vain pääkäyttäjä"))
    }
  }
}
