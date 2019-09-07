package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet

class KoesuoritusPDFServlet(implicit val application: KoskiApplication) extends HtmlServlet {
  private val koesuoritukset: KoesuoritusService = KoesuoritusService(application.config)

  get("/:copyOfExamPaper") {
    contentType = "application/pdf"
    koesuoritukset.writeKoesuoritus(getStringParam("copyOfExamPaper"), response.getOutputStream)
  }
}
