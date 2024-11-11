package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{omaDataKumppani, omaDataOAuth2Scope, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_MYDATA_LISAYS}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import java.util.UUID.randomUUID

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

        oauth2Repository.create(code, koskiSession.oid, clientId, scope, codeChallenge, redirectUri) match {
          case Right(_) =>
            AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_MYDATA_LISAYS, koskiSession, Map(
              oppijaHenkiloOid -> koskiSession.oid,
              omaDataKumppani -> clientId,
              omaDataOAuth2Scope -> scope
            )))
            Right(code)
          case Left(error) => Left(error)
        }
    }
  }

  def generateSecret: String = {
    randomUUID.toString.replaceAll("-", "")
  }
}
