package fi.oph.koski.suoritusjako

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, NoCache}

import scala.util.Try

class SuoritusjakoServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKansalainen with Logging with NoCache {
  get("/:uuid") {
    renderEither(application.suoritusjakoService.get(params("uuid")))
  }

  put("/") {
    withJsonBody({ body =>
      Try(JsonSerializer.extract[List[SuoritusIdentifier]](body)).toOption match {
        case Some(suoritusIds) =>
          renderEither(application.suoritusjakoService.put(koskiSession.oid, suoritusIds).right.map(SuoritusjakoResponse))
        case None =>
          haltWithStatus(KoskiErrorCategory.badRequest.format())
      }
    })()
  }
}

case class SuoritusjakoResponse(uuid: String)
