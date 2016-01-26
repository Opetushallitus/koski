package fi.oph.tor.organisaatio

import fi.oph.tor.schema.KoodistoKoodiViite

object MockOrganisaatiot {
  val omnomnia = OrganisaatioHierarkia("1.2.246.562.10.51720121923", Some(KoodistoKoodiViite("10054", None, "oppilaitosnumero", None)), "Omnian ammattiopisto", List("OPPILAITOS"), Nil)
  val stadinAmmattiopisto = OrganisaatioHierarkia("1.2.246.562.10.52251087186", Some(KoodistoKoodiViite("10105", None, "oppilaitosnumero", None)), "Stadin ammattiopisto", List("OPPILAITOS"), List(
    OrganisaatioHierarkia("1.2.246.562.10.42456023292", None, "Stadin ammattiopisto, Lehtikuusentien toimipaikka", List("TOIMIPISTE"), Nil)))
  val winnova = OrganisaatioHierarkia("1.2.246.562.10.37144658251", Some(KoodistoKoodiViite("10095", None, "oppilaitosnumero", None)), "Winnova", List("OPPILAITOS"), Nil)

  val oppilaitokset = List(
    stadinAmmattiopisto,
    omnomnia,
    winnova
  )

  val organisaatiot = oppilaitokset ++ List(OrganisaatioHierarkia("1.2.246.562.10.346830761110", None, "Helsingin kaupunki", List("KOULUTUSTOIMIJA"), Nil))
}

object MockOrganisaatioRepository extends InMemoryOrganisaatioRepository(MockOrganisaatiot.organisaatiot) {
}