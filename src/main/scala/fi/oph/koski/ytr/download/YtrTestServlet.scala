package fi.oph.koski.ytr.download

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificAuthenticationSupport
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.sso.KoskiSpecificSSOSupport

import java.time.LocalDate

class YtrTestServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with KoskiSpecificSSOSupport with NoCache {
  private val downloadService = application.ytrDownloadService

  get("/download") {
    logger.info("Download YTR started")
    downloadService.download(
      birthdateStart = getOptionalStringParam("birthdateStart").map(LocalDate.parse),
      birthdateEnd = getOptionalStringParam("birthdateEnd").map(LocalDate.parse),
      modifiedSince = getOptionalStringParam("modifiedSince").map(LocalDate.parse),
      force = getOptionalBooleanParam("force").getOrElse(false),
      onEnd = () => {
        logger.info("Download YTR done")
      }
    )
    renderObject(Map("status" -> "loading"))
  }

  get("/download-status") {
    // TODO: Tarkista, että operaatio on oikeasti valmis
    renderObject(Map("status" -> "done"))
  }
}
