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
      case _ => throw new InvalidRequestException("query parameter lenght must be at least 3")
    }
  }
}
