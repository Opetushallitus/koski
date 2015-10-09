package fi.oph.tor.user

import fi.oph.tor.organisaatio.{OrganisaatioPuu, Organisaatio}

class MockUserRepository extends UserRepository {
  def getUserOrganisations(oid: String): OrganisaatioPuu = OrganisaatioPuu(List(
    Organisaatio("1", "Helsingin Ammattioppilaitos", List("OPPILAITOS"), Nil),
    Organisaatio("2", "Metropolia Helsinki", List("OPPILAITOS"), Nil),
    Organisaatio("3", "Omnia Helsinki", List("OPPILAITOS"), Nil)
  ))
}



