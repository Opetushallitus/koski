package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKansalainen}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.log.KoskiOperation.{KoskiOperation, YTR_YOTODISTUKSEN_LUONTI, YTR_YOTODISTUKSEN_LATAAMINEN}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.ChainingSyntax._

class YoTodistusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with RequiresKansalainen {

  val service: YoTodistusService = application.yoTodistusService

  get("/status/:lang/:oppijaOid") {
    renderEither(getRequest.flatMap(service.currentStatus))
  }

  get("/generate/:lang/:oppijaOid") {
    renderEither(
      getRequest
        .flatMap(service.initiateGenerating)
        .tap(_ => mkAuditLog(session, YTR_YOTODISTUKSEN_LUONTI))
    )
  }

  get("/download/:lang/:oppijaOid/:filename") {
    getRequest.flatMap(service.currentStatus) match {
      case Right(state: YtrCertificateCompleted) =>
        mkAuditLog(session, YTR_YOTODISTUKSEN_LATAAMINEN)
        contentType = "application/pdf"
        service.download(state, response.getOutputStream)
      case _ =>
        renderStatus(KoskiErrorCategory.unavailable.yoTodistus.notCompleteOrNoAccess())
    }
  }

  private def getRequest: Either[HttpStatus, YoTodistusOidRequest] =
    Right(YoTodistusOidRequest(
      oid = params("oppijaOid"),
      language = params("lang"),
    )).flatMap(checkAccess)

  private def checkAccess(req: YoTodistusOidRequest): Either[HttpStatus, YoTodistusOidRequest] =
    if (session.oid == req.oid || session.isUsersHuollettava(req.oid)) {
      Right(req)
    } else {
      Left(KoskiErrorCategory.unauthorized())
    }

  private def mkAuditLog(session: KoskiSpecificSession, operation: KoskiOperation): Unit = mkAuditLog(session.oid, operation)
  private def mkAuditLog(oid: String, operation: KoskiOperation): Unit = AuditLog.log(KoskiAuditLogMessage(operation, session, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oid)))
}
