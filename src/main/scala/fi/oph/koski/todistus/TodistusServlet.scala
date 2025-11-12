package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AuthenticationSupport, HasKoskiSpecificSession, KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession, RequiresKansalainen, RequiresSession, UserLanguage}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.log.KoskiOperation.{KoskiOperation, TODISTUKSEN_LATAAMINEN, TODISTUKSEN_LUONTI}
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.ChainingSyntax._

import java.util.UUID
import scala.util.{Try, Using}

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

  private def requireOphPääkäyttäjä: Unit = {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
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
      todistusJob <- validateCompletedTodistus
      stream <- service.getDownloadStream(BucketType.STAMPED, todistusJob)
    } yield (stream, todistusJob)

    result.fold(renderStatus, r => {
      val (stream, todistusJob) = r
      val filename = generateFilename(todistusJob)
      auditLogTodistusDownload(todistusJob)

      contentType = "application/pdf"
      response.setHeader("Content-Disposition", s"""attachment; filename="$filename"""")

      Using.resource(stream) { inputStream =>
        val outputStream = response.getOutputStream
        inputStream.transferTo(outputStream)
        outputStream.flush()
      }
    })
  }

  get("/download-presigned/:id") {
    requireOphPääkäyttäjä

    val result = for {
      todistusJob <- validateCompletedTodistus
      filename = generateFilename(todistusJob)
      url <- service.getDownloadUrl(BucketType.STAMPED, filename, todistusJob)
    } yield (url, todistusJob)

    result.fold(renderStatus, r => {
      val (url, todistusJob) = r
      auditLogTodistusDownload(todistusJob)

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

  private def validateCompletedTodistus: Either[HttpStatus, TodistusJob] = {
    for {
      req <- getIdRequest
      todistusJob <- service.currentStatus(req)
      _ <- Either.cond(
        todistusJob.state == TodistusState.COMPLETED,
        (),
        KoskiErrorCategory.unavailable.todistus.notCompleteOrNoAccess()
      )
    } yield todistusJob
  }

  private def auditLogTodistusDownload(todistusJob: TodistusJob): Unit = {
    mkAuditLog(
      operation = TODISTUKSEN_LATAAMINEN,
      extraFields = Map(
        KoskiAuditLogMessageField.oppijaHenkiloOid -> todistusJob.oppijaOid,
        KoskiAuditLogMessageField.opiskeluoikeusOid -> todistusJob.opiskeluoikeusOid,
        KoskiAuditLogMessageField.opiskeluoikeusVersio -> todistusJob.opiskeluoikeusVersionumero.map(_.toString).getOrElse(""),
        KoskiAuditLogMessageField.todistusId -> todistusJob.id
      )
    )
  }

  private def generateFilename(todistusJob: TodistusJob): String = {
    val defaultFilename = s"todistus_${todistusJob.language}.pdf"

    application.possu.findByOidIlmanKäyttöoikeustarkistusta(todistusJob.opiskeluoikeusOid) match {
      case Right(opiskeluoikeus) =>
        if (opiskeluoikeus.suoritustyypit.isEmpty) {
          logger.warn(s"Opiskeluoikeudelta ${todistusJob.opiskeluoikeusOid} puuttuu suoritustyyppi, käytetään default-tiedostonimeä")
          defaultFilename
        } else if (opiskeluoikeus.suoritustyypit.size > 1) {
          generateFilenameWithLocalization(opiskeluoikeus.koulutusmuoto, todistusJob)
        } else {
          generateFilenameWithLocalization(opiskeluoikeus.koulutusmuoto, opiskeluoikeus.suoritustyypit.head, todistusJob)
        }
      case Left(error) =>
        logger.warn(s"Opiskeluoikeuden ${todistusJob.opiskeluoikeusOid} haku epäonnistui: ${error.errorString.mkString(", ")}, käytetään default-tiedostonimeä")
        defaultFilename
    }
  }

  private def generateFilenameWithLocalization(koulutusmuoto: String, suoritustyyppi: String, todistusJob: TodistusJob): String = {
    generateFilenameWithLocalizationFromKey(s"${koulutusmuoto}_${suoritustyyppi}_todistus_", todistusJob)
  }

  private def generateFilenameWithLocalization(koulutusmuoto: String, todistusJob: TodistusJob): String = {
    generateFilenameWithLocalizationFromKey(s"${koulutusmuoto}_todistus_", todistusJob)
  }

  private def generateFilenameWithLocalizationFromKey(keyPart: String, todistusJob: TodistusJob): String = {
    val uiLang = UserLanguage.getLanguageFromCookie(request)
    val key = s"todistus_download_tiedostonimi:$keyPart"
    val localizations = application.koskiLocalizationRepository.localizations

    val prefix = if (localizations.contains(key)) {
      localizations(key).get(uiLang)
    } else {
      keyPart
    }

    s"${prefix}${todistusJob.language}.pdf"
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
