package fi.oph.tor.security

import fi.oph.tor.user.{UserRepository, UserContext}

trait RequiresAuthentication extends CurrentUser {
  def userRepository: UserRepository

  implicit def userContext: UserContext = new UserContext {
    def organisaatioPuu = userRepository.getUserOrganisations(getAuthenticatedUser.get.oid)
  }

  before() {
    if(getAuthenticatedUser.isEmpty) {
      halt(401, "Not authenticated")
    }
  }
}
