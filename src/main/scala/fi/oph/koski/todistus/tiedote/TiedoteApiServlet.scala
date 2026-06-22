package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.KoskiAuditLogMessageField.{opiskeluoikeusOid => opiskeluoikeusOidField, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.TIEDOTE_RESETOITU
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class TiedoteApiServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with RequiresSession
{
  before() {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
    }
  }

  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val service = application.kielitutkintotodistusTiedoteService

  get("/jobs") {
    val state = params.get("state")
    val limit = params.get("limit").map(_.toInt).getOrElse(100)
    val offset = params.get("offset").map(_.toInt).getOrElse(0)

    renderObject(repository.findAll(limit, offset, state))
  }

  get("/stats") {
    renderObject(repository.countByState)
  }

  get("/run") {
    if (!application.config.getBoolean("tiedote.enabled")) {
      haltWithStatus(KoskiErrorCategory.notImplemented("Tiedotepalvelu ei ole käytössä"))
    }

    application.kielitutkintotodistusTiedoteScheduler.schedulerInstance match {
      case Some(scheduler) =>
        scheduler.triggerNow()
        renderObject(Map("status" -> "triggered"))
      case None =>
        haltWithStatus(KoskiErrorCategory.notImplemented("Tiedote-scheduler ei ole käynnissä"))
    }
  }

  delete("/jobs/:opiskeluoikeusOid") {
    val opiskeluoikeusOid = params("opiskeluoikeusOid")

    repository.setDeletedByOpiskeluoikeusOid(opiskeluoikeusOid) match {
      case Some(job) =>
        AuditLog.log(KoskiAuditLogMessage(TIEDOTE_RESETOITU, session, Map(
          oppijaHenkiloOid -> job.oppijaOid,
          opiskeluoikeusOidField -> opiskeluoikeusOid
        )))
        renderObject(job)
      case None =>
        haltWithStatus(KoskiErrorCategory.notFound("Tiedotetta ei löytynyt opiskeluoikeudelle"))
    }
  }
}
