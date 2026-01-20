package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresOmaDataOAuth2}
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, OAUTH2_KATSOMINEN_KAIKKI_TIEDOT, OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import scala.reflect.runtime.universe.TypeTag

class OmaDataOAuth2ResourceServerServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
  with Logging with ContentEncodingSupport with NoCache with RequiresOmaDataOAuth2 with ConvertErrorsToOAuth2Format {
  // in: access token
  // out: data, jos käyttäjällä oikeudet kyseiseen access tokeniin.
  //      TAI OAuth2-protokollan mukainen virheilmoitus
  post("/") {
    request.header("Authorization").map(_.split(" ")) match {
      case Some(Array("Bearer", token)) =>
        renderRequestedData(token)
      case _ =>
        renderError(OmaDataOAuth2ErrorType.invalid_request, "Request missing valid token parameters", msg => logger.warn(msg))
    }
  }

  private def renderRequestedData(token: String): Unit = {
    application.omaDataOAuth2Service.getByAccessToken(
      accessToken = token,
      expectedClientId = koskiSession.user.username,
      allowedScopes = koskiSession.omaDataOAuth2Scopes
    ) match {
      case Right(AccessTokenInfo(_, tokenExpirationTime, oppijaOid, scope)) =>
        renderOpinnot(oppijaOid, scope, tokenExpirationTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
      case Left(error) =>
        val errorResult = error.getAccessTokenErrorResponse
        renderErrorWithStatus(errorResult, errorResult.httpStatus)
    }
  }

  private def renderOpinnot(oppijaOid: String, scope: String, tokenExpirationTime: String): Unit = {
    val overrideSession = KoskiSpecificSession.oauth2KatsominenUser(request)

    scope.split(" ").filter(_.startsWith("OPISKELUOIKEUDET_")).toSeq match {
      case Seq("OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT") =>
        val oppija = application.omaDataOAuth2Service.findSuoritetutTutkinnot(oppijaOid, scope, overrideSession, tokenExpirationTime)
        auditLogKatsominen(OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT, koskiSession.user.username, koskiSession, oppijaOid, scope)
        renderOppijaData(oppija)
      case Seq("OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT") =>
        val oppija = application.omaDataOAuth2Service.findAktiivisetJaPäättyneetOpinnot(oppijaOid, scope, overrideSession, tokenExpirationTime)
        auditLogKatsominen(OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, koskiSession.user.username, koskiSession, oppijaOid, scope)
        renderOppijaData(oppija)
      case Seq("OPISKELUOIKEUDET_KAIKKI_TIEDOT") =>
        val oppija = application.omaDataOAuth2Service.findKaikkiTiedot(oppijaOid, scope, overrideSession, tokenExpirationTime)
        auditLogKatsominen(OAUTH2_KATSOMINEN_KAIKKI_TIEDOT, koskiSession.user.username, koskiSession, oppijaOid, scope)
        renderOppijaData(oppija)
      case _ =>
        renderError(OmaDataOAuth2ErrorType.server_error, s"Internal error, unable to handle OPISKELUOIKEUDET scope defined in ${scope}", msg => logger.error(msg))
    }
  }

  private def auditLogKatsominen(
    operation: KoskiOperation.KoskiOperation,
    expectedClientId: String,
    koskiSession: KoskiSpecificSession,
    oppijaOid: String,
    scope: String
  ): Unit = {
    AuditLog.log(KoskiAuditLogMessage(operation, koskiSession, Map(
      oppijaHenkiloOid -> oppijaOid,
      omaDataKumppani -> expectedClientId,
      omaDataOAuth2Scope -> scope
    )))
  }

  private def renderOppijaData[T: TypeTag](oppija: Either[HttpStatus, T]): Unit = oppija match {
    case Right(oppija) =>
      renderObject(oppija)
    case Left(httpStatus) =>
      renderError(OmaDataOAuth2ErrorType.server_error, httpStatus, "Internal error", msg => logger.warn(msg))
  }

  private def renderError(errorType: OmaDataOAuth2ErrorType, message: String, log: String => Unit): Unit = {
    val errorResult = logAndCreateError(errorType, message, log)
    renderErrorWithStatus(errorResult, errorResult.httpStatus)
  }

  private def renderError(errorType: OmaDataOAuth2ErrorType, httpStatus: HttpStatus, message: String, log: String => Unit): Unit = {
    val errorResult = logAndCreateError(errorType, httpStatus.errorString.getOrElse(message), log)
    renderErrorWithStatus(errorResult, httpStatus.statusCode)
    renderObject(errorResult)
  }

  private def logAndCreateError(errorType: OmaDataOAuth2ErrorType, message: String, log: String => Unit) = {
    val error = OmaDataOAuth2Error(errorType, message)
    log(error.getLoggedErrorMessage)
    error.getAccessTokenErrorResponse
  }

  private def renderErrorWithStatus(errorResult: OAuth2ErrorResponse, status: Int): Unit = {
    response.setStatus(status)
    renderObject(errorResult)
  }
}
