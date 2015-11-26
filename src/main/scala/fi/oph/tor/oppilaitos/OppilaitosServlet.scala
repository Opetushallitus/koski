package fi.oph.tor.oppilaitos

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.json.Json
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.user.UserRepository
import fi.vm.sade.security.ldap.DirectoryClient

class OppilaitosServlet(oppilaitosRepository: OppilaitosRepository, val userRepository: UserRepository, val directoryClient: DirectoryClient) extends ErrorHandlingServlet with RequiresAuthentication {
  get("/") {
    contentType = "application/json;charset=utf-8"
    Json.write(oppilaitosRepository.oppilaitokset)
  }
}
