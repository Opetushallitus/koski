package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AuthenticationSupport, HasKoskiSpecificSession, KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession, RequiresKansalainen, RequiresSession}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.log.KoskiOperation.{KoskiOperation, TODISTUKSEN_LATAAMINEN, TODISTUKSEN_LUONTI}
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.ChainingSyntax._

import java.util.UUID
import scala.util.Try

class TodistusServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
    with HasKoskiSpecificSession
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  val service: TodistusService = application.todistusService

  before() {
    requireKansalainenOrOphPääkäyttäjä
  }

  private def requireKansalainenOrOphPääkäyttäjä: Unit = {
    getUser match {
      case Right(user) if user.kansalainen || session.hasRole(OPHPAAKAYTTAJA) => // OK
      case Right(_) => haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain kansalaiselle tai OPH-pääkäyttäjälle"))
      case Left(error) => haltWithStatus(error)
    }
  }

  get("/status/:id") {
    renderEither(
      getIdRequest
        .flatMap(service.currentStatus)
    )
  }

  get("/status/:lang/:opiskeluoikeusOid") {
    renderEither(
      getTodistusGenerateRequest
        .flatMap(service.checkStatus)
    )
  }

  get("/generate/:lang/:opiskeluoikeusOid") {
    renderEither(
      getTodistusGenerateRequest
        .flatMap(service.checkAccessAndInitiateGenerating)
        .tap(todistusJob => mkAuditLog(
          operation = TODISTUKSEN_LUONTI,
          extraFields = Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> todistusJob.oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid -> todistusJob.opiskeluoikeusOid,
            KoskiAuditLogMessageField.todistusId -> todistusJob.id
          )
        ))
    )
  }

  get("/download/:id") {
    val result = for {
      req <- getIdRequest
      todistusJob <- service.currentStatus(req) // Tekee myös käyttöoikeustarkistuksen
      _ <- Either.cond(
        todistusJob.state == TodistusState.COMPLETED,
        (),
        KoskiErrorCategory.unavailable.todistus.notCompleteOrNoAccess()
      )

      url <- service.getDownloadUrl(BucketType.STAMPED, todistusJob)
    } yield (url, todistusJob)

    result.fold(renderStatus, r => {
      val (url, todistusJob) = r

      mkAuditLog(
        operation = TODISTUKSEN_LATAAMINEN,
        extraFields = Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid -> todistusJob.oppijaOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid -> todistusJob.opiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio -> todistusJob.opiskeluoikeusVersionumero.map(_.toString).getOrElse(""),
          KoskiAuditLogMessageField.todistusId -> todistusJob.id
        )
      )

      contentType = "application/pdf"
      redirect(url)
    })
  }

  private def getIdRequest: Either[HttpStatus, TodistusIdRequest] = {
    val id = params("id")
    Try(UUID.fromString(id)).toEither match {
      case Right(_) => Right(TodistusIdRequest(id))
      case Left(_) => Left(KoskiErrorCategory.badRequest(s"Virheellinen UUID: $id"))
    }
  }

  private def getTodistusGenerateRequest: Either[HttpStatus, TodistusGenerateRequest] = {
    val lang = params("lang")
    val oid = params("opiskeluoikeusOid")

    if (!TodistusLanguage.*.contains(lang)) {
      Left(KoskiErrorCategory.badRequest(s"Virheellinen kieli: $lang. Sallitut arvot: ${TodistusLanguage.*.mkString(", ")}"))
    } else if (!Opiskeluoikeus.isValidOpiskeluoikeusOid(oid)) {
      Left(KoskiErrorCategory.badRequest(s"Virheellinen opiskeluoikeus OID: $oid"))
    } else {
      Right(TodistusGenerateRequest(
        opiskeluoikeusOid = oid,
        language = lang,
      ))
    }
  }

  private def mkAuditLog(
    operation: KoskiOperation,
    extraFields: AuditLogMessage.ExtraFields = Map.empty
  )(implicit user: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(operation, user, extraFields))
}

case class TodistusGenerateRequest(
  opiskeluoikeusOid: String,
  language: String,
) {
  def toPathParams = s"${language}/${opiskeluoikeusOid}"
}

case class TodistusIdRequest(
  id: String
)
