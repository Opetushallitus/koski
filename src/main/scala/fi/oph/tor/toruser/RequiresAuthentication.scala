package fi.oph.tor.toruser

import fi.oph.tor.ErrorHandlingServlet

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserOrganisationsRepository

  implicit def torUser: TorUser = {
    val user = TorUser(userOption.get.oid, userRepository.getUserOrganisations(userOption.get.oid))
    println(user)
    user
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}