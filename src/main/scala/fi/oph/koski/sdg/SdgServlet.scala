package fi.oph.koski.sdg

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresSdg}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

case class HetuRequest(hetu: String)

case class SdgQueryParams(withOsasuoritukset: Boolean = false, onlyVahvistetut: Boolean = false)

class SdgServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresSdg with NoCache {
  post("/hetu") {
    val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)

    val withOsasuoritukset = params.getAs[Boolean]("withOsasuoritukset").getOrElse(false)
    val onlyVahvistetut = params.getAs[Boolean]("vainVahvistetut").getOrElse(false)

    val queryParams = SdgQueryParams(withOsasuoritukset, onlyVahvistetut)

    withJsonBody { json =>
      val result = for {
        hetu <- extractAndValidateHetu(json)
        oppija <- application.sdgService.findOppijaByHetu(hetu, queryParams)(ophKatselijaUser)
      } yield {
        logOppijaAccess(oppija.henkilö.oid)
        oppija
      }

      renderEither(result)
    }()
  }

  private def extractAndValidateHetu(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[HetuRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))

  private def logOppijaAccess(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit =
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
