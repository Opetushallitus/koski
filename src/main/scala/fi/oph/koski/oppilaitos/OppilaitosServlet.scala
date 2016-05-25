package fi.oph.koski.oppilaitos

import fi.oph.koski.servlet.ApiServlet
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class OppilaitosServlet(oppilaitosRepository: OppilaitosRepository, val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient) extends ApiServlet with RequiresAuthentication {
  get("/") {
    oppilaitosRepository.oppilaitokset(koskiUser).toList
  }
}
