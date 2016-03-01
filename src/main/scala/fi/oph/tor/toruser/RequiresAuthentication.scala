package fi.oph.tor.toruser

import fi.oph.tor.servlet.ErrorHandlingServlet

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  def torUser: TorUser = {
    TorUser(userOption.get.oid, userRepository.getUserOrganisations(userOption.get.oid))
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}