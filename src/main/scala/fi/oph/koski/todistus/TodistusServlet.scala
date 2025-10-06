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

  get("/status/:id") {
    // TODO: TOR-2400: Pitäisikö myös status-kysely audit-lokittaa?
    renderEither(
      getIdRequest
        .flatMap(service.currentStatus)
    )
  }

  get("/status/:lang/:oppijaOid/:opiskeluoikeusOid") {
    // TODO: TOR-2400: Katso, onko oppijalla jo ajantasainen (oo versio sama, henkilötiedot hash jne.) todistus saatavilla, jolloin
    // sitä ei tarvitse luoda uudestaan. Voi olla, että helpompi vaan
    renderEither(Left(KoskiErrorCategory.notImplemented()))
  }

  get("/generate/:lang/:oppijaOid/:opiskeluoikeusOid") {
    // TODO: TOR-2400: Estä generointi ja palauta olemassaolevan todistuksen valmiit tiedot, jos tarpeeksi tuore allekirjoitettu todistus samalla sisällöllä on jo olemassa
    renderEither(
      getTodistusGenerateRequestAndCheckAccess
        .flatMap(service.initiateGenerating)
        .tap(_ => mkAuditLog(session, TODISTUKSEN_LUONTI))
    )
  }

  get("/download/:id") {
    (getIdRequest.flatMap(service.currentStatus) match {
      case Right(todistusJob: TodistusJob) if todistusJob.state == "COMPLETED" =>
        mkAuditLog(session, TODISTUKSEN_LATAAMINEN)
        contentType = "application/pdf"
        service.getDownloadUrl(BucketType.STAMPED, todistusJob)
      case _ =>
        Left(KoskiErrorCategory.unavailable.todistus.notCompleteOrNoAccess())
    })
      .fold(renderStatus, redirect)
  }

  private def getIdRequest: Either[HttpStatus, TodistusIdRequest] =
    Right(TodistusIdRequest(
      id = params("id")
    ))

  private def getTodistusGenerateRequestAndCheckAccess: Either[HttpStatus, TodistusGenerateRequest] =
    Right(TodistusGenerateRequest(
      oppijaOid = params("oppijaOid"),
      opiskeluoikeusOid = params("opiskeluoikeusOid"),
      language = params("lang"),
    )).flatMap(checkAccess)

  private def checkAccess(req: TodistusGenerateRequest): Either[HttpStatus, TodistusGenerateRequest] =
    if (session.oid == req.oppijaOid || session.isUsersHuollettava(req.oppijaOid)) {
      // TODO: TOR-2400: tarkista myös, että pyydetty opiskeluoikeus on oppijan oma?
      Right(req)
    } else {
      Left(KoskiErrorCategory.forbidden())
    }

  private def mkAuditLog(session: KoskiSpecificSession, operation: KoskiOperation): Unit = mkAuditLog(session.oid, operation)
  private def mkAuditLog(oid: String, operation: KoskiOperation): Unit = AuditLog.log(KoskiAuditLogMessage(operation, session, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oid)))
}

case class TodistusGenerateRequest(
  oppijaOid: String,
  opiskeluoikeusOid: String,
  language: String,
) {
  def toPathParams = s"${language}/${oppijaOid}/${opiskeluoikeusOid}"
}

case class TodistusIdRequest(
  id: String
)
