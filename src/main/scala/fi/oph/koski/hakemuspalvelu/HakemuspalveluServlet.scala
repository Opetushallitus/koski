package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresHakemuspalvelu}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.vkt.VktKoskeenTallennettavaOpiskeluoikeus
import org.json4s.JValue

case class OidRequest(oid: String)

class HakemuspalveluServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresHakemuspalvelu with NoCache {
  post("/oid") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      val oppija = extractAndValidateOid(json).flatMap(oid => application.hakemuspalveluService.findOppija(oid)(ophKatselijaUser))
      oppija.map(o => o.opiskeluoikeudet.foreach {
        case x: HakemuspalveluKoskeenTallennettavaOpiskeluoikeus if x.oid.isDefined => auditLog(o.henkilö.oid, opiskeluoikeusOid = x.oid.get)
        case _ => auditLog(o.henkilö.oid)
      })
      renderEither(oppija)
    }()
  }

  private def extractAndValidateOid(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[OidRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid))

  private def auditLog(oppijaOid: String, opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.HAKEMUSPALVELU_OPISKELUOIKEUS_HAKU,
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
          KoskiOperation.HAKEMUSPALVELU_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
          )
        )
      )
}
