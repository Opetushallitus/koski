package fi.oph.tor.toruser

import fi.oph.tor.organisaatio.{InMemoryOrganisaatioRepository, MockOrganisaatiot}

class MockUserOrganisationsRepository extends UserOrganisationsRepository {
  val users: Map [String, InMemoryOrganisaatioRepository] = Map(
    "12345" -> new InMemoryOrganisaatioRepository(MockOrganisaatiot.oppilaitokset),
    "11111" -> new InMemoryOrganisaatioRepository(List(MockOrganisaatiot.omnomnia)))

  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository = users.getOrElse(oid, InMemoryOrganisaatioRepository.empty)
}



