package fi.oph.koski.organisaatio

import org.scalatest.{FreeSpec, Matchers}

class OrganisaatioHierarkiaSpec extends FreeSpec with Matchers {
  "OrganisaatioHierarkia can be flattened" in {
    val pyhtäänHierarkia: List[OrganisaatioHierarkia] = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.pyhtäänKunta).toList
    val expectedOids = extractOids(pyhtäänHierarkia).distinct.sorted
    OrganisaatioHierarkia.flatten(pyhtäänHierarkia).map(_.oid).sorted should equal(expectedOids)
  }

  def extractOids(hierarkiat: List[OrganisaatioHierarkia]): List[String] = if (hierarkiat.nonEmpty) {
    hierarkiat.map(_.oid) ++ extractOids(hierarkiat.flatMap(_.children))
  } else {
    Nil
  }
}
