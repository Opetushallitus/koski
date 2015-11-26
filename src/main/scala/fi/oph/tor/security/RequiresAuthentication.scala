package fi.oph.tor.security

import fi.oph.tor.ErrorHandlingServlet
import fi.oph.tor.oppilaitos.Oppilaitos
import fi.oph.tor.organisaatio.OrganisaatioPuu
import fi.oph.tor.user.{UserRepository, UserContext}

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def userRepository: UserRepository

  implicit def userContext: UserContext = new UserContext {
    def organisaatioPuu = userOption
      .map(u => userRepository.getUserOrganisations(u.oid))
      .getOrElse(OrganisaatioPuu(List.empty))

    override def hasReadAccess(organisaatio: Oppilaitos) = {
      organisaatioPuu.findById(organisaatio.oid).isDefined
    }
  }

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}
