package fi.oph.tor.oppija

import fi.oph.tor.json.Json
import fi.oph.tor.security.Authenticated
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.utils.slf4j.Logging

class OppijaServlet(oppijaRepository: OppijaRepository) extends ErrorHandlingServlet with Logging with Authenticated {
  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) => Json.write(oppijaRepository.findOppijat(query))
      case _ => throw new InvalidRequestException("query parameter length must be at least 3")
    }
  }

  post("/") {
    contentType = "text/plain;charset=utf-8"
    val oppija: CreateOppija = Json.read[CreateOppija](request.body)
    val result: OppijaCreationResult = oppijaRepository.create(oppija)
    halt(result.httpStatus, result.text)
  }
}
