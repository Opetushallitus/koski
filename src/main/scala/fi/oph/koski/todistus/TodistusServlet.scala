package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.koskiuser.{HasKoskiSpecificSession, KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession, UserLanguage}
import fi.oph.koski.log.KoskiOperation.{TODISTUKSEN_LATAAMINEN, KoskiOperation}
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiAuditLogMessage, KoskiAuditLogMessageField, Logging}
import fi.oph.koski.schema.Opiskeluoikeus
import org.scalatra.ScalatraServlet

import java.util.UUID
import scala.util.Try

trait TodistusServlet extends ScalatraServlet with HasKoskiSpecificSession with KoskiCookieAndBasicAuthenticationSupport with Logging {
  def application: KoskiApplication
  implicit def session: KoskiSpecificSession

  val service: TodistusService = application.todistusService

  protected def requireKansalainenOrOphPääkäyttäjä: Unit = {
    getUser match {
      case Right(user) if user.kansalainen || session.hasRole(OPHPAAKAYTTAJA) => // OK
      case Right(_) => haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain kansalaiselle tai OPH-pääkäyttäjälle"))
      case Left(error) => haltWithStatus(error)
    }
  }

  protected def requireOphPääkäyttäjä: Unit = {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
    }
  }

  protected def getIdRequest: Either[HttpStatus, TodistusIdRequest] = {
    val id = params("id")
    Try(UUID.fromString(id)).toEither match {
      case Right(_) => Right(TodistusIdRequest(id))
      case Left(_) => Left(KoskiErrorCategory.badRequest(s"Virheellinen UUID: $id"))
    }
  }

  protected def getTodistusGenerateRequest: Either[HttpStatus, TodistusGenerateRequest] = {
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

  protected def validateCompletedTodistus: Either[HttpStatus, TodistusJob] = {
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

  protected def auditLogTodistusDownload(todistusJob: TodistusJob): Unit = {
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

  protected def generateFilename(todistusJob: TodistusJob): String = {
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

  protected def mkAuditLog(
    operation: KoskiOperation,
    extraFields: AuditLogMessage.ExtraFields = Map.empty
  )(implicit user: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(operation, user, extraFields))
}
