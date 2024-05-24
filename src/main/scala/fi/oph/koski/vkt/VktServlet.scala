package fi.oph.koski.vkt

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

class VktServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/oid") {
    withJsonBody { json =>
      renderEither(extractAndValidateOid(json).flatMap(application.vktService.findOppija))
    }()
  }

  private def extractAndValidateOid(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[OidRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid))

  private case class OidRequest(oid: String)
}
