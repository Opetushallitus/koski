package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresSuomiFi
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JsonAST.JValue

class SuomiFiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresSuomiFi with NoCache {
  private val suomiFiService = new SuomiFiService(application)

  post("/rekisteritiedot") {
    withJsonBody { json =>
      renderEither(
        SuomiFiRequest.parse(json)
          .flatMap(hetu => application.hetu.validate(hetu))
          .flatMap(hetu => suomiFiService.suomiFiOpiskeluoikeudet(hetu))
      )
    }()
  }
}

object SuomiFiRequest {
  def parse(json: JValue): Either[HttpStatus, String] = {
    JsonSerializer.validateAndExtract[SuomiFiRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(req => Hetu.validFormat(req.hetu))
  }
}

case class SuomiFiRequest(hetu: String)
