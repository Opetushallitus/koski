package fi.oph.tor.organisaatio

object MockOrganisaatiot {
  val omnomnia = OrganisaatioHierarkia("3", "Omnia Helsinki", List("OPPILAITOS"), Nil)
  val helsinginAmmattiOpisto = OrganisaatioHierarkia("1", "Helsingin Ammattioppilaitos", List("OPPILAITOS"), List(OrganisaatioHierarkia("1.2.246.562.10.42456023292", "Stadin ammattiopisto, Lehtikuusentien toimipaikka", List("TOIMIPISTE"), Nil)))

  // TODO: replace with actual data, real OIDs
  val oppilaitokset = List(
    helsinginAmmattiOpisto,
    OrganisaatioHierarkia("2", "Metropolia Helsinki", List("OPPILAITOS"), Nil),
    omnomnia,
    OrganisaatioHierarkia("1.2.246.562.10.52251087186", "Stadin Ammattiopisto", List("OPPILAITOS"), Nil)
  )

  val organisaatiot = oppilaitokset ++ List(OrganisaatioHierarkia("1.2.246.562.10.346830761110", "Helsingin kaupunki", List("KOULUTUSTOIMIJA"), Nil))
}

object MockOrganisaatioRepository extends InMemoryOrganisaatioRepository(MockOrganisaatiot.organisaatiot) {
}