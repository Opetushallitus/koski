package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_MYDATA_LISAYS, OAUTH2_ACCESS_TOKEN_LUONTI, OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT, OAUTH2_KATSOMINEN_KAIKKI_TIEDOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.generateSecret
import fi.oph.koski.util.ChainingSyntax.eitherChainingOps

class OmaDataOAuth2Service(oauth2Repository: OmaDataOAuth2Repository, val application: KoskiApplication) extends Logging {

  var overridenCreateResultForUnitTests: Option[Either[OmaDataOAuth2Error, String]] = None

  def create(
    clientId: String,
    scope: String,
    codeChallenge: String,
    redirectUri: String,
    koskiSession: KoskiSpecificSession
  ): Either[OmaDataOAuth2Error, String] = {
    overridenCreateResultForUnitTests match {
      case Some(overridenResult) =>
        overridenResult
      case _ =>
        val code = generateSecret

        oauth2Repository.create(code, koskiSession.oid, clientId, scope, codeChallenge, redirectUri)
          .tap(_ =>
            AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_MYDATA_LISAYS, koskiSession, Map(
              oppijaHenkiloOid -> koskiSession.oid,
              omaDataKumppani -> clientId,
              omaDataOAuth2Scope -> scope
            ))))
          .map(_ => code)
    }
  }

  def createAccessTokenForCode(
    code: String,
    expectedClientId: String,
    expectedCodeChallenge: String,
    expectedRedirectUri: Option[String],
    koskiSession: KoskiSpecificSession,
    allowedScopes: Set[String]
  ): Either[OmaDataOAuth2Error, AccessTokenSuccessResponse] = {
    oauth2Repository.createAccessTokenForCode(code, expectedClientId, expectedCodeChallenge, expectedRedirectUri, allowedScopes)
      .tap(response =>
        AuditLog.log(KoskiAuditLogMessage(OAUTH2_ACCESS_TOKEN_LUONTI, koskiSession, Map(
          oppijaHenkiloOid -> response.oppijaOid,
          omaDataKumppani -> expectedClientId,
          omaDataOAuth2Scope -> response.scope
        )))
      )
      .map(_.successResponse)
  }

  def getByAccessToken(
    accessToken: String,
    expectedClientId: String,
    koskiSession: KoskiSpecificSession,
    allowedScopes: Set[String]
  ): Either[OmaDataOAuth2Error, AccessTokenSuccessResponse] = {
    oauth2Repository.getByAccessToken(accessToken, expectedClientId, allowedScopes)
      .flatMap(response => {
        response.scope.split(" ").filter(_.startsWith("OPISKELUOIKEUDET_")).toSeq match {
          case Seq("OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT") =>
            auditLogKatsominen(OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT, expectedClientId, koskiSession, response)
            Right(response)
          case Seq("OPISKELUOIKEUDET_AKTIIVISET_JA_PAATTYNEET_OPINNOT") =>
            auditLogKatsominen(OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, expectedClientId, koskiSession, response)
            Right(response)
          case Seq("OPISKELUOIKEUDET_KAIKKI_TIEDOT") =>
            auditLogKatsominen(OAUTH2_KATSOMINEN_KAIKKI_TIEDOT, expectedClientId, koskiSession, response)
            Right(response)
          case _ =>
            val error =
              OmaDataOAuth2Error(OmaDataOAuth2ErrorType.server_error, s"Internal error, unable to handle OPISKELUOIKEUDET scope defined in ${response.scope}")
            logger.error(error.getLoggedErrorMessage)
            Left(error)
        }
      })
      .map(_.successResponse)
  }

  private def auditLogKatsominen(
    operation: KoskiOperation.KoskiOperation,
    expectedClientId: String,
    koskiSession: KoskiSpecificSession,
    response: AccessTokenInfo
  ): Unit = {
    AuditLog.log(KoskiAuditLogMessage(operation, koskiSession, Map(
      oppijaHenkiloOid -> response.oppijaOid,
      omaDataKumppani -> expectedClientId,
      omaDataOAuth2Scope -> response.scope
    )))
  }
}
