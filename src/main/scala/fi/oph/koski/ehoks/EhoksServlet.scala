package fi.oph.koski.ehoks

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiAuditLogMessageField.{oppijaHenkiloOid, opiskeluoikeusOid}
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_KATSOMINEN
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

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
    val result = for {
      alku <- parseTimestamp("alku", getOptionalStringParam("alku"))
      loppu <- parseTimestamp("loppu", getOptionalStringParam("loppu").orElse(Some(OffsetDateTime.now().toString)))
      _ <- Either.cond(alku.before(loppu), (), KoskiErrorCategory.badRequest.queryParam("alku pitää olla ennen loppua"))
    } yield application.ehoksService.mitätöidytOpiskeluoikeudet(alku, loppu)
    renderEither[Seq[String]](result)
  }

  private def parseTimestamp(name: String, value: Option[String]): Either[HttpStatus, Timestamp] =
    value
      .toRight(KoskiErrorCategory.badRequest.queryParam(s"Parametri $name puuttuu"))
      .flatMap { s =>
        try {
          Right(Timestamp.from(OffsetDateTime.parse(s).toInstant))
        } catch {
          case _: DateTimeParseException =>
            Left(KoskiErrorCategory.badRequest.format.pvm(s"Parametrin $name pitää olla ISO 8601 -aikaleima aikavyöhykkeellä, esim. 2026-01-01T00:00:00+02:00"))
        }
      }
}
