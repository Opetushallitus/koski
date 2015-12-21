package fi.oph.tor.security

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.organisaatio.UserOrganisations
import fi.oph.tor.user.{UserContext, UserRepository}

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserRepository

  implicit def userContext: UserContext = new UserContext {
    def userOrganisations: UserOrganisations = userOption
      .map(u => userRepository.getUserOrganisations(u.oid))
      .getOrElse(UserOrganisations.empty)
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}
