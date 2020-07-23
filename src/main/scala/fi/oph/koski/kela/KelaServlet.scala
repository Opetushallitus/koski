package fi.oph.koski.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.schema.Henkilö
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JsonAST.JValue

class KelaServlet(implicit val application: KoskiApplication) extends ApiServlet  with  RequiresLuovutuspalvelu with NoCache {
  val kelaService = new KelaService(application)

  post("/hetu") {
    withJsonBody { json =>
      val hetu = KelaRequest.parse(json)
      val oppija = hetu.flatMap(kelaService.findKelaOppijaByHetu(_)(koskiSession))
      renderEither(oppija)
    }()
  }
}

object KelaRequest {
  def parse(json: JValue): Either[HttpStatus, Henkilö.Hetu] = {
    JsonSerializer.validateAndExtract[KelaRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))
  }
}

case class KelaRequest(hetu: String)
