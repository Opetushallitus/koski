package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.HtmlServlet

class YtrKoesuoritusServlet(implicit val application: KoskiApplication) extends HtmlServlet with RequiresKansalainen {
  private val koesuoritukset: KoesuoritusService = KoesuoritusService(application.config)

  get("/:copyOfExamPaper") {
    val examPaper = getStringParam("copyOfExamPaper")
    val hasAccess = hasAccessTo(examPaper)
    if (koesuoritukset.koesuoritusExists(examPaper) && hasAccess) {
      contentType = "application/pdf"
      koesuoritukset.writeKoesuoritus(examPaper, response.getOutputStream)
    } else {
      logger.warn(s"Exam paper $examPaper not found, hasAccess: $hasAccess")
      haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy())
    }
  }

  private def hasAccessTo(examPaper: String): Boolean = {
    logger.debug(s"Tarkistetaan ${if (isHuollettava) "huollettavan" else "oma"} koesuoritus access")
    getOppija.flatMap(application.ytrRepository.findByTunnisteet)
      .exists(_.examPapers.contains(examPaper))
  }

  private def getOppija: Option[HenkilönTunnisteet] = if (isHuollettava) {
    application.huoltajaService.getHuollettava(koskiSession.oid)
  } else {
    application.henkilöRepository.findByOid(koskiSession.oid)
  }

  private def isHuollettava = getBooleanParam("huollettava", false)
}
