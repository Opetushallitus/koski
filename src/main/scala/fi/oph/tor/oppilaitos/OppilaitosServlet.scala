package fi.oph.tor.oppilaitos

import fi.oph.tor.{InvalidRequestException, ErrorHandlingServlet}
import fi.oph.tor.json.Json

class OppilaitosServlet(oppilaitosRepository: OppilaitosRepository) extends ErrorHandlingServlet {
  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) => Json.write(oppilaitosRepository.findOppilaitokset(query))
      case _ => throw new InvalidRequestException("query parameter length must be at least 3")
    }
  }
}
