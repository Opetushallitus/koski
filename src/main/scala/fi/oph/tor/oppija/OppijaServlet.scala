package fi.oph.tor.oppija

import fi.oph.tor.json.Json
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.utils.slf4j.Logging

class OppijaServlet extends ErrorHandlingServlet with Logging {

  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("nimi") match {
      case Some(nimi) => Json.write(List(Oppija("esimerkki", "eero", "010101-123N")))
      case None => throw new InvalidRequestException("Missing query parameter")
    }
  }

}
