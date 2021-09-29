package fi.oph.koski.organisaatio

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OrganisaatioHierarkiaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
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
