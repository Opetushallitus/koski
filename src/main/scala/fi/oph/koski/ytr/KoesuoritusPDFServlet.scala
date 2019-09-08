package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.HtmlServlet

class KoesuoritusPDFServlet(implicit val application: KoskiApplication) extends HtmlServlet with RequiresKansalainen {
  private val koesuoritukset: KoesuoritusService = KoesuoritusService(application.config)

  get("/:copyOfExamPaper") {
    val examPaper = getStringParam("copyOfExamPaper")
    if (koesuoritukset.koesuoritusExists(examPaper) && hasAccessTo(examPaper)) {
      contentType = "application/pdf"
      koesuoritukset.writeKoesuoritus(examPaper, response.getOutputStream)
    } else {
      logger.warn(s"Exam paper $examPaper not found for user ${koskiSession.oid}")
      haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy())
    }
  }


  private def hasAccessTo(examPaper: String): Boolean =
    application.henkilöRepository.findByOid(koskiSession.oid)
      .flatMap(application.ytrRepository.findByTunnisteet)
      .exists(_.examPapers.contains(examPaper))
}
