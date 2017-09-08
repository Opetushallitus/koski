package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.servlet.{ApiServletRequiringAuthentication, NoCache}

class OpiskeluoikeusServlet(implicit val application: KoskiApplication) extends ApiServletRequiringAuthentication with Logging with NoCache {
  get("/:oid") {
    val result: Either[HttpStatus, OpiskeluoikeusRow] = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(koskiSession)
    result.map(oo => AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(oppijaHenkiloOid -> oo.oppijaOid))).foreach(AuditLog.log)
    renderEither(result.map(_.toOpiskeluoikeus))
  }
}