package fi.oph.tor.toruser

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  implicit def userContext: TorUser = new UserImpl(userOption.get, userRepository)

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}

class UserImpl(val user: AuthenticationUser, userRepository: UserOrganisationsRepository) extends TorUser {
  lazy val userOrganisations: InMemoryOrganisaatioRepository = userRepository.getUserOrganisations(user.oid)
}