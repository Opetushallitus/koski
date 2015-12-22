package fi.oph.tor.toruser

import fi.oph.tor.organisaatio.{InMemoryOrganisaatioRepository, MockOrganisaatioRepository}

class MockUserOrganisationsRepository extends UserOrganisationsRepository {
  val users: Map [String, InMemoryOrganisaatioRepository] = Map(
    "12345" -> new InMemoryOrganisaatioRepository(MockOrganisaatioRepository.oppilaitokset),
    "11111" -> new InMemoryOrganisaatioRepository(List(MockOrganisaatioRepository.omnomnia)))

  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository = users.getOrElse(oid, InMemoryOrganisaatioRepository.empty)
}



