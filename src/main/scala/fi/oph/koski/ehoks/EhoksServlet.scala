package fi.oph.koski.ehoks

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiAuditLogMessageField.{oppijaHenkiloOid, opiskeluoikeusOid}
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_KATSOMINEN
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

import java.time.LocalDate

class EhoksServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with RequiresVirkailijaOrPalvelukäyttäjä
    with NoCache {

  before() {
    if (!koskiSessionOption.exists(_.hasGlobalReadAccess)) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
  }

  get("/oppija/:oid") {
    val result = HenkilöOid.validateHenkilöOid(getStringParam("oid"))
      .flatMap(oid => application.ehoksService.findOppija(oid)(session))
    result.foreach { oppija =>
      AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, session, Map(
        oppijaHenkiloOid -> oppija.henkilö.oid,
        opiskeluoikeusOid -> oppija.opiskeluoikeudet.map(_.oid).mkString(",")
      )))
    }
    renderEither[EhoksOppija](result)
  }

  get("/opiskeluoikeus/:oid") {
    val result = application.ehoksService.findOpiskeluoikeus(getStringParam("oid"))(session)
    result.foreach { case (oppijaOid, oo) =>
      AuditLog.log(KoskiAuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, session, Map(
        oppijaHenkiloOid -> oppijaOid,
        opiskeluoikeusOid -> oo.oid
      )))
    }
    renderEither[EhoksOpiskeluoikeus](result.map(_._2))
  }

  get("/mitatoidyt") {
    val alku = getLocalDateParam("alku")
    val loppu = if (params.contains("loppu")) getLocalDateParam("loppu") else LocalDate.now()
    val result = Either.cond(
      !alku.isAfter(loppu),
      application.ehoksService.mitätöidytOpiskeluoikeudet(alku, loppu),
      KoskiErrorCategory.badRequest.queryParam("alku ei voi olla loppua myöhemmin")
    )
    renderEither[Seq[String]](result)
  }
}
