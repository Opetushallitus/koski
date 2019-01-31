package fi.oph.koski.etk

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JsonAST.JValue

class ElaketurvakeskusServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {

  val elaketurvakeskusService = new ElaketurvakeskusService(application)

  before() {
    if (request.getRemoteHost != "127.0.0.1") {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }

  post("/ammatillisetperustutkinnot") {
    withJsonBody { parsedJson =>
      parseEtkTutkintotietoRequest(parsedJson) match {
        case Left(status) => haltWithStatus(status)
        case Right(req) => renderObject(elaketurvakeskusService.ammatillisetPerustutkinnot(req.vuosi, req.alku, req.loppu))
      }
    }()
  }

  private def parseEtkTutkintotietoRequest(parsedJson: JValue) = {
    JsonSerializer.validateAndExtract[EtkTutkintotietoRequest](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
  }
}

case class EtkTutkintotietoRequest(alku: LocalDate, loppu: LocalDate, vuosi: Int)

