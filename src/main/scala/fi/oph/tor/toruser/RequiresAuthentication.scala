package fi.oph.tor.toruser

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  implicit def userContext: TorUser = new TorUser {
    def userOrganisations: InMemoryOrganisaatioRepository = userOption
      .map(u => userRepository.getUserOrganisations(u.oid))
      .getOrElse(InMemoryOrganisaatioRepository.empty)

    override def user = scentry.user
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}
