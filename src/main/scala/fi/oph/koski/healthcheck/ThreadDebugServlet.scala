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
      val traces = java.lang.Thread.getAllStackTraces.values

      import collection.JavaConverters._

      for (a2 <- traces.asScala) {
        if (!a2.exists(a => a.toString.contains("jdk.internal.misc.Unsafe.park"))) {
          logger.info("========== DEBUG STACK TRACE\n    " + a2.map(_.toString).mkString("\n    "))
        }
      }

      renderStatus(HttpStatus.ok)
    } else {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Vain pääkäyttäjä"))
    }
  }
}
