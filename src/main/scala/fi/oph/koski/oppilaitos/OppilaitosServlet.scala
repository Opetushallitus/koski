package fi.oph.koski.oppilaitos

import fi.oph.koski.koskiuser.{RequiresAuthentication, KäyttöoikeusRepository}
import fi.oph.koski.servlet.ApiServlet
import fi.vm.sade.security.ldap.DirectoryClient

class OppilaitosServlet(oppilaitosRepository: OppilaitosRepository, val käyttöoikeudet: KäyttöoikeusRepository, val directoryClient: DirectoryClient) extends ApiServlet with RequiresAuthentication {
  get("/") {
    oppilaitosRepository.oppilaitokset(koskiUser).toList
  }
}
