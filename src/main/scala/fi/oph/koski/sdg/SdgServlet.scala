package fi.oph.koski.sdg

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresSdg}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

case class HetuRequest(hetu: String)

class SdgServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresSdg with NoCache {
  post("/hetu") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    val includeOsasuoritukset = params.getAs[Boolean]("includeOsasuoritukset").getOrElse(false)

    withJsonBody { json =>
      val result = for {
        hetu <- extractAndValidateHetu(json)
        oppija <- application.sdgService.findOppijaByHetu(hetu, includeOsasuoritukset)(ophKatselijaUser)
      } yield {
        auditLog(oppija.henkilö.oid)
        oppija
      }

      renderEither(result)
    }()
  }

  private def extractAndValidateHetu(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[HetuRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))

  private def auditLog(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog
      .log(
        KoskiAuditLogMessage(
          KoskiOperation.SDG_OPISKELUOIKEUS_HAKU,
          user,
          Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid,
          )
        )
      )
}
