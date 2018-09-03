package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OpiskeluoikeusServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {
  get("/:oid") {
    val result: Either[HttpStatus, OpiskeluoikeusRow] = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(koskiSession)
    result.map(oo => AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(oppijaHenkiloOid -> oo.oppijaOid))).foreach(AuditLog.log)
    renderEither[KoskeenTallennettavaOpiskeluoikeus](result.map(_.toOpiskeluoikeus))
  }

  delete("/:oid/:index") {
    val result = application.oppijaFacade.invalidatePäätasonSuoritus(getStringParam("oid"), getIntegerParam("index"))
    result.foreach(_ => application.elasticSearch.refreshIndex)
    renderEither[HenkilönOpiskeluoikeusVersiot](result)
  }

  delete("/:oid") {
    val result = application.oppijaFacade.invalidateOpiskeluoikeus(getStringParam("oid"))
    result.foreach(_ => application.elasticSearch.refreshIndex)
    renderEither[HenkilönOpiskeluoikeusVersiot](result)
  }
}
