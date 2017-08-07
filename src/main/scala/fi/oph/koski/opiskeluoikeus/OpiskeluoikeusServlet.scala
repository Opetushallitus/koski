package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OpiskeluoikeusServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with NoCache {
  get("/:oid") {
    val result: Option[OpiskeluoikeusRow] = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(koskiSession)
    renderEither(result match {
      case Some(oo) =>
        AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(oppijaHenkiloOid -> oo.oppijaOid)))
        Right(oo.toOpiskeluoikeus)
      case _ => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    })
  }
}