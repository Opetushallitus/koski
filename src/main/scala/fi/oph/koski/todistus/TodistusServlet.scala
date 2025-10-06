package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKansalainen}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.log.KoskiOperation.{KoskiOperation, TODISTUKSEN_LATAAMINEN, TODISTUKSEN_LUONTI}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.ChainingSyntax._

class TodistusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with RequiresKansalainen {

  val service: TodistusService = application.todistusService

  get("/status/:lang/:oppijaOid/:opiskeluoikeusOid") {
    renderEither(
      getRequestAndCheckAccess
        .flatMap(service.currentStatus)
      // TODO: TOR-2400: Pitäisikö myös status-kysely audit-lokittaa?
    )
  }

  get("/generate/:lang/:oppijaOid/:opiskeluoikeusOid") {
    renderEither(
      getRequestAndCheckAccess
        .flatMap(service.initiateGenerating)
        .tap(_ => mkAuditLog(session, TODISTUKSEN_LUONTI))
    )
  }

  get("/download/:lang/:oppijaOid/:filename") {
    // TODO: TOR-2400: tämä mahdollisesti muuttuu, jos käytetään presigned S3 URL:ia?
    getRequestAndCheckAccess.flatMap(service.currentStatus) match {
      case Right(state: CertificateCompleted) =>
        mkAuditLog(session, TODISTUKSEN_LATAAMINEN)
        contentType = "application/pdf"
        service.downloadCertificate(state, response.getOutputStream)
      case _ =>
        renderStatus(KoskiErrorCategory.unavailable.todistus.notCompleteOrNoAccess())
    }
  }

  private def getRequestAndCheckAccess: Either[HttpStatus, TodistusOidRequest] =
    Right(TodistusOidRequest(
      oppijaOid = params("oppijaOid"),
      opiskeluoikeusOid = params("opiskeluoikeusOid"),
      language = params("lang"),
    )).flatMap(checkAccess)

  private def checkAccess(req: TodistusOidRequest): Either[HttpStatus, TodistusOidRequest] =
    if (session.oid == req.oppijaOid || session.isUsersHuollettava(req.oppijaOid)) {
      // TODO: TOR-2400: tarkista myös, että pyydetty opiskeluoikeus on oppijan oma?
      Right(req)
    } else {
      Left(KoskiErrorCategory.unauthorized())
    }

  private def mkAuditLog(session: KoskiSpecificSession, operation: KoskiOperation): Unit = mkAuditLog(session.oid, operation)
  private def mkAuditLog(oid: String, operation: KoskiOperation): Unit = AuditLog.log(KoskiAuditLogMessage(operation, session, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oid)))
}

case class TodistusOidRequest(
  oppijaOid: String,
  opiskeluoikeusOid: String,
  language: String,
)
