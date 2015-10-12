package fi.oph.tor.user

import fi.oph.tor.organisaatio.{OrganisaatioPuu, Organisaatio}

class MockUserRepository extends UserRepository {
  val users: Map [String, OrganisaatioPuu] = Map(
    "12345" -> OrganisaatioPuu(List(
      Organisaatio("1", "Helsingin Ammattioppilaitos", List("OPPILAITOS"), Nil),
      Organisaatio("2", "Metropolia Helsinki", List("OPPILAITOS"), Nil),
      Organisaatio("3", "Omnia Helsinki", List("OPPILAITOS"), Nil)
    )),
    "11111" -> OrganisaatioPuu(List(
      Organisaatio("3", "Omnia Helsinki", List("OPPILAITOS"), Nil)
    ))
  )

  def getUserOrganisations(oid: String): OrganisaatioPuu = users.getOrElse(oid, OrganisaatioPuu(Nil))
}



