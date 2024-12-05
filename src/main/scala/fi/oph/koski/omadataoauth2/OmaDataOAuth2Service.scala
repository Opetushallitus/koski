package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_MYDATA_LISAYS, KANSALAINEN_MYDATA_POISTO, OAUTH2_ACCESS_TOKEN_LUONTI}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiOperation, Logging}
import fi.oph.koski.omadataoauth2.OmaDataOAuth2ErrorType.invalid_request
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.generateSecret
import fi.oph.koski.schema.{LocalizedString, Opiskeluoikeus, Oppija, TäydellisetHenkilötiedot}
import fi.oph.koski.util.ChainingSyntax.eitherChainingOps

class OmaDataOAuth2Service(oauth2Repository: OmaDataOAuth2Repository, val application: KoskiApplication) extends Logging with OmaDataOAuth2Config {

  var overridenCreateResultForUnitTests: Option[Either[OmaDataOAuth2Error, String]] = None

  def create(
    clientId: String,
    scope: String,
    codeChallenge: String,
    redirectUri: String,
    koskiSession: KoskiSpecificSession
  ): Either[OmaDataOAuth2Error, String] = {

    val codeChallengeExists = oauth2Repository.getByCodeChallenge(codeChallenge, clientId).isDefined

    val tokenDurationMinutes = getTokenDurationMinutes(clientId)

    overridenCreateResultForUnitTests match {
      case Some(overridenResult) =>
        overridenResult
      case _ if codeChallengeExists =>
        val error = OmaDataOAuth2Error(invalid_request, "Invalid code challenge")
        logger.warn(s"${error.getLoggedErrorMessage}, same code challenge already used by client")
        Left(error)
      case _ =>
        val code = generateSecret

        oauth2Repository.create(code, koskiSession.oid, clientId, scope, codeChallenge, redirectUri, tokenDurationMinutes)
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
    allowedScopes: Set[String]
  ): Either[OmaDataOAuth2Error, AccessTokenInfo] = {
    oauth2Repository.getByAccessToken(accessToken, expectedClientId, allowedScopes)
  }

  def findSuoritetutTutkinnot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Either[HttpStatus, OmaDataOAuth2SuoritetutTutkinnot] = {
    application.suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = false
    )(overrideSession).map(oppija => {
      OmaDataOAuth2SuoritetutTutkinnot(
        henkilö = OmaDataOAuth2Henkilötiedot(oppija.henkilö, scope),
        opiskeluoikeudet = oppija.opiskeluoikeudet
      )
    })
  }

  def getActiveRows(session: KoskiSpecificSession): Seq[OmaDataOAuth2JakoItem] = {

    application.henkilöRepository.findByOid(session.oid, findMasterIfSlaveOid = true).map(_.kaikkiOidit).toSeq.flatMap(kaikkiOidit =>
      oauth2Repository.getActiveRows(kaikkiOidit).map(r => OmaDataOAuth2JakoItem(
        codeSHA256 = r.codeSHA256,
        clientId = r.clientId,
        clientName = getClientName(r.clientId),
        expirationDate = r.voimassaAsti.toLocalDateTime,
        creationDate = r.luotu.toLocalDateTime,
        scope = r.scope
      ))
    )
  }

  def revokeConsent(session: KoskiSpecificSession, codeSHA256: String): HttpStatus = {
    val row = oauth2Repository.getBySHA256(codeSHA256)
    val allOids = application.henkilöRepository.findByOid(session.oid, findMasterIfSlaveOid = true).map(_.kaikkiOidit).getOrElse(Nil)
    row match {
      case None => OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, "No consent found with code").toKoskiHttpStatus
      case Some(row) if !allOids.contains(row.oppijaOid) => OmaDataOAuth2Error(OmaDataOAuth2ErrorType.invalid_request, "No consent found with code").toKoskiHttpStatus
      case Some(row) =>
        oauth2Repository.invalidateBySHA256(codeSHA256, "Consent revoked by resource owner")
        AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_MYDATA_POISTO, session, Map(
          oppijaHenkiloOid -> row.oppijaOid,
          omaDataKumppani -> row.clientId,
          omaDataOAuth2Scope -> row.scope
        )))
        HttpStatus.ok
    }
  }

  def getClientName(clientId: String): LocalizedString = {
    application.koodistoPalvelu.getKoodistoKoodit(application.koodistoPalvelu.getLatestVersionRequired("omadataoauth2client"))
      .find(_.koodiArvo == clientId)
      .flatMap(_.nimi)
      .getOrElse(LocalizedString.unlocalized(clientId))
  }

  def findAktiivisetJaPäättyneetOpinnot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Either[HttpStatus, OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet] = {
    application.aktiivisetJaPäättyneetOpinnotService.findAktiivisetJaPäättyneetOpinnotOppija(
      oppijaOid,
      merkitseSuoritusjakoTehdyksi = false
    )(overrideSession).map(oppija => {
      OmaDataOAuth2AktiivisetJaPäättyneetOpiskeluoikeudet(
        henkilö = OmaDataOAuth2Henkilötiedot(oppija.henkilö, scope),
        opiskeluoikeudet = oppija.opiskeluoikeudet
      )
    })
  }

  def findKaikkiTiedot(oppijaOid: String, scope: String, overrideSession: KoskiSpecificSession): Either[HttpStatus, OmaDataOAuth2KaikkiOpiskeluoikeudet] = {
    application.oppijaFacade.findOppija(oppijaOid)(overrideSession).flatMap(_.warningsToLeft) match {
      case Right(Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet: Seq[Opiskeluoikeus])) =>
        Right(OmaDataOAuth2KaikkiOpiskeluoikeudet(
          henkilö = OmaDataOAuth2Henkilötiedot(henkilö, scope),
          opiskeluoikeudet = opiskeluoikeudet.toList
        ))
      case Right(_) =>
        Left(KoskiErrorCategory.internalError("Datatype not recognized"))
      case Left(httpStatus) =>
        Left(httpStatus)
    }
  }
}

