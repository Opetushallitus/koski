package fi.oph.koski.vkt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresVkt}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

case class OidRequest(oid: String)
case class HetuRequest(hetu: String)

class VktServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVkt with NoCache {
  post("/oid") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      val oppija = extractAndValidateOid(json).flatMap(oid => application.vktService.findOppija(oid)(ophKatselijaUser))
      oppija.map(o => o.opiskeluoikeudet.foreach {
        case x: VktKoskeenTallennettavaOpiskeluoikeus if x.oid.isDefined => auditLog(o.henkilö.oid, opiskeluoikeusOid = x.oid.get)
        case _ => auditLog(o.henkilö.oid)
      })
      renderEither(oppija)
    }()
  }

  post("/hetu") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      renderEither(extractAndValidateHetu(json).flatMap(hetu => application.vktService.findOppijaByHetu(hetu)(ophKatselijaUser)))
    }()
  }

  private def extractAndValidateOid(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[OidRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid))

  private def extractAndValidateHetu(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[HetuRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))

  private def auditLog(oppijaOid: String, opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.VKT_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid -> opiskeluoikeusOid,
          )
        )
      )

  private def auditLog(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.VKT_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
          )
        )
      )
}
