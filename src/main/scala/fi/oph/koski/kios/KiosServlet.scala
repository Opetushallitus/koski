package fi.oph.koski.kios

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKios}
import fi.oph.koski.log.AuditLogMessage.ExtraFields
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

case class OidRequest(oid: String)
case class HetuRequest(hetu: String)

class KiosServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresKios with NoCache {
  post("/oid") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      val oppija = extractAndValidateOid(json).flatMap(oid => application.kiosService.findOppija(oid)(ophKatselijaUser))
      oppija.foreach(auditLog)
      renderEither(oppija)
    }()
  }

  post("/hetu") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      val oppija = extractAndValidateHetu(json).flatMap(hetu => application.kiosService.findOppijaByHetu(hetu)(ophKatselijaUser))
      oppija.foreach(auditLog)
      renderEither(oppija)
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

  private def auditLog(oppija: KiosOppija)(implicit user: KoskiSpecificSession): Unit = {
    val oidit = oppija.opiskeluoikeudet.collect {
      case x: KiosKoskeenTallennettavaOpiskeluoikeus if x.oid.isDefined => x.oid.get
    }
    val base: ExtraFields = Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)
    val fields =
      if (oidit.nonEmpty) base + (KoskiAuditLogMessageField.opiskeluoikeusOid -> oidit.mkString(","))
      else base
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.KIOS_OPISKELUOIKEUS_HAKU, user, fields))
  }
}
