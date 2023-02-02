package fi.oph.koski.ytr.download

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.sso.KoskiSpecificSSOSupport

import java.time.LocalDate

class YtrTestServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with KoskiSpecificSSOSupport with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val downloadService = application.ytrDownloadService

  get("/download") {
    logger.info("Download YTR started")
    downloadService.download(
      birthmonthStart = getOptionalStringParam("birthmonthStart")
        .map(m => {
          LocalDate.parse(m + "-01")
          m
        }),
      birthmonthEnd = getOptionalStringParam("birthmonthEnd")
        .map(m => {
          LocalDate.parse(m + "-01")
          m
        }),
      modifiedSince = getOptionalStringParam("modifiedSince").map(LocalDate.parse),
      force = getOptionalBooleanParam("force").getOrElse(false),
      onEnd = () => {
        logger.info("Download YTR done")
      }
    )
    renderObject(downloadService.status.getDownloadStatusJson)
  }
}
