package fi.oph.tor.user

import fi.oph.tor.organisaatio.{UserOrganisations, OrganisaatioHierarkia}

class MockUserRepository extends UserRepository {
  val users: Map [String, UserOrganisations] = Map(
    "12345" -> UserOrganisations(List(
      OrganisaatioHierarkia("1", "Helsingin Ammattioppilaitos", List("OPPILAITOS"), Nil),
      OrganisaatioHierarkia("2", "Metropolia Helsinki", List("OPPILAITOS"), Nil),
      OrganisaatioHierarkia("3", "Omnia Helsinki", List("OPPILAITOS"), Nil),
      OrganisaatioHierarkia("1.2.246.562.10.52251087186", "Stadin Ammattiopisto", List("OPPILAITOS"), Nil)
    )),
    "11111" -> UserOrganisations(List(
      OrganisaatioHierarkia("3", "Omnia Helsinki", List("OPPILAITOS"), Nil)
    ))
  )

  def getUserOrganisations(oid: String): UserOrganisations = users.getOrElse(oid, UserOrganisations.empty)
}



