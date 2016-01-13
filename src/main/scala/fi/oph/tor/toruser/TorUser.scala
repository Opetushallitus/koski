package fi.oph.tor.toruser

import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

class TorUser(user: AuthenticationUser, userRepository: UserOrganisationsRepository) {
  lazy val userOrganisations: InMemoryOrganisaatioRepository = userRepository.getUserOrganisations(user.oid)
  val oid = user.oid
}