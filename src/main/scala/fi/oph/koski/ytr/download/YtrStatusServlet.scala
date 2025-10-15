package fi.oph.koski.ytr.download

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.sso.KoskiSpecificSSOSupport

import java.time.LocalDate

class YtrStatusServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiCookieAndBasicAuthenticationSupport with KoskiSpecificSSOSupport with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val downloadService = application.ytrDownloadService

  get("/download-status") {
    renderObject(downloadService.status.getDownloadStatusJsonLatest)
  }
}
