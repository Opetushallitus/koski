package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class YoTodistusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with RequiresKansalainen {

  val service: YoTodistusService = application.yoTodistusService

  get("/status/:lang/:oppijaOid") {
    renderEither(getRequest.flatMap(service.currentStatus))
  }

  get("/generate/:lang/:oppijaOid") {
    renderEither(getRequest.flatMap(service.initiateGenerating))
  }

  get("/download/:lang/:oppijaOid/:filename") {
    getRequest.flatMap(service.currentStatus) match {
      case Right(state: YtrCertificateCompleted) =>
        contentType = "application/pdf"
        service.download(state, response.getOutputStream)
      case _ =>
        renderStatus(KoskiErrorCategory.unavailable.yoTodistus.notCompleteOrNoAccess())
    }
  }

  def getRequest: Either[HttpStatus, YoTodistusOidRequest] = {
    // TODO: Tarkasta käyttöoikeudet
    Right(YoTodistusOidRequest(oid = params("oppijaOid"), language = params("lang")))
  }
}
