package fi.oph.tor.user

import fi.oph.tor.organisaatio.UserOrganisations

trait UserContext {
  def userOrganisations: UserOrganisations
}
