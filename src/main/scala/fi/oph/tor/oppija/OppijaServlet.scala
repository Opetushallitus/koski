package fi.oph.tor.oppija

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.tor.{TodennetunOsaamisenRekisteri, TorOppija}
import fi.oph.tor.user.UserRepository
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.utils.slf4j.Logging

class OppijaServlet(rekisteri: TodennetunOsaamisenRekisteri)(implicit val userRepository: UserRepository) extends ErrorHandlingServlet with Logging with RequiresAuthentication {

  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        Json.write(rekisteri.findOppijat(query))
      case _ => throw new InvalidRequestException("query parameter length must be at least 3")
    }
  }

  get("/:oid") {
    renderEither(rekisteri.userView(params("oid")))
  }

  post("/") {
    getClass.synchronized{
      val oppija: TorOppija = Json.read[TorOppija](request.body)
      renderEither(rekisteri.createOrUpdate(oppija))
    }
  }
}
