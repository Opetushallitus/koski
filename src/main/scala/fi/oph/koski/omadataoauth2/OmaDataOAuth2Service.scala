package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_MYDATA_LISAYS, OAUTH2_ACCESS_TOKEN_LUONTI}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.omadataoauth2.OmaDataOAuth2Security.generateSecret
import fi.oph.koski.schema.{Opiskeluoikeus, Oppija, TäydellisetHenkilötiedot}
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

