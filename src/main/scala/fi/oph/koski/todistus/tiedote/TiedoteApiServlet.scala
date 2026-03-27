package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.KoskiAuditLogMessageField.{opiskeluoikeusOid => opiskeluoikeusOidField, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.TIEDOTE_RESETOITU
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class TiedoteApiServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

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
    if (application.kielitutkintotodistusTiedoteScheduler.schedulerInstance.exists(_.isTaskRunning)) {
      haltWithStatus(KoskiErrorCategory.conflict("Tiedotteiden käsittely on jo käynnissä"))
    }

    application.kielitutkintotodistusTiedoteScheduler.schedulerInstance match {
      case Some(scheduler) if scheduler.hasLease =>
        scheduler.triggerNow()
        renderObject(Map("status" -> "triggered"))
      case Some(_) =>
        // Tämä endpointti on lähinnä testaamista varten. TODO toteuta triggerNow käyttämään kantaa koordinoidusti jos tulee tarpeen.
        haltWithStatus(KoskiErrorCategory.unavailable("Tiedote-scheduler ei ole aktiivinen tällä instanssilla. Kokeile pyyntöä uudelleen, jotta se ohjautuisi aktiiviselle instanssille."))
      case None =>
        haltWithStatus(KoskiErrorCategory.notImplemented("Tiedote-scheduler ei ole käynnissä"))
    }
  }

  delete("/jobs/:opiskeluoikeusOid") {
    val opiskeluoikeusOid = params("opiskeluoikeusOid")

    repository.deleteByOpiskeluoikeusOid(opiskeluoikeusOid) match {
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
