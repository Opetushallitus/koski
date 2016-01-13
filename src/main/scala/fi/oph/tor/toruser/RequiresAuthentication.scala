package fi.oph.tor.toruser

import fi.oph.tor.ErrorHandlingServlet

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  implicit def torUser: TorUser = new TorUser(userOption.get, userRepository)

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}