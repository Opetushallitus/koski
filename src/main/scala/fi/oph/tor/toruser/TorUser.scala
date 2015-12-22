package fi.oph.tor.toruser

import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

trait TorUser {
  def user: AuthenticationUser
  def userOrganisations: InMemoryOrganisaatioRepository
}
