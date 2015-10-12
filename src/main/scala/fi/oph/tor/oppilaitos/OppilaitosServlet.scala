package fi.oph.tor.oppilaitos

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.user.UserRepository

class OppilaitosServlet(oppilaitosRepository: OppilaitosRepository)(implicit val userRepository: UserRepository) extends ErrorHandlingServlet with RequiresAuthentication {
  get("/") {
    contentType = "application/json;charset=utf-8"
    Json.write(oppilaitosRepository.oppilaitokset)
  }
}
