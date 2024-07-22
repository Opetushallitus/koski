package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresHakemuspalvelu}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JValue

case class OidRequest(oid: String)

class HakemuspalveluServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresHakemuspalvelu with NoCache {
  post("/oid") {
    implicit val ophKatselijaUser = KoskiSpecificSession.ophKatselijaUser(request)
    withJsonBody { json =>
      renderEither(extractAndValidateOid(json).flatMap(oid => application.hakemuspalveluService.findOppija(oid)(ophKatselijaUser)))
    }()
  }

  private def extractAndValidateOid(json: JValue): Either[HttpStatus, String] =
    JsonSerializer.validateAndExtract[OidRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid))
}
