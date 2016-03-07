package fi.oph.tor.toruser

import fi.oph.tor.servlet.ErrorHandlingServlet

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  def torUser: TorUser = {
    TorUser(userOption.get.oid, userRepository)
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}