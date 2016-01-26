package fi.oph.tor.organisaatio

import fi.oph.tor.schema.KoodistoKoodiViite

object MockOrganisaatiot {
  val omnomnia = OrganisaatioHierarkia("3", Some(KoodistoKoodiViite("23456", None, "oppilaitosnumero", None)), "Omnia Helsinki", List("OPPILAITOS"), Nil)
  val helsinginAmmattiOpisto = OrganisaatioHierarkia("1", Some(KoodistoKoodiViite("12345", None, "oppilaitosnumero", None)), "Helsingin Ammattioppilaitos", List("OPPILAITOS"), List(
    OrganisaatioHierarkia("1.2.246.562.10.42456023292", None, "Stadin ammattiopisto, Lehtikuusentien toimipaikka", List("TOIMIPISTE"), Nil)))

  // TODO: replace with actual data, real OIDs
  val oppilaitokset = List(
    helsinginAmmattiOpisto,
    OrganisaatioHierarkia("2", Some(KoodistoKoodiViite("10065", None, "oppilaitosnumero", None)), "Metropolia Helsinki", List("OPPILAITOS"), Nil),
    omnomnia,
    OrganisaatioHierarkia("1.2.246.562.10.52251087186", Some(KoodistoKoodiViite("10105", None, "oppilaitosnumero", None)), "Stadin Ammattiopisto", List("OPPILAITOS"), Nil),
    OrganisaatioHierarkia("1.2.246.562.10.37144658251", Some(KoodistoKoodiViite("10095", None, "oppilaitosnumero", None)), "Winnova", List("OPPILAITOS"), Nil)
  )

  val organisaatiot = oppilaitokset ++ List(OrganisaatioHierarkia("1.2.246.562.10.346830761110", None, "Helsingin kaupunki", List("KOULUTUSTOIMIJA"), Nil))
}

object MockOrganisaatioRepository extends InMemoryOrganisaatioRepository(MockOrganisaatiot.organisaatiot) {
}