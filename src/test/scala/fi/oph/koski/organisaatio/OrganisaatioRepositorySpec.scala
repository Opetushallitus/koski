package fi.oph.koski.organisaatio

import org.scalatest.{FreeSpec, Matchers}

class OrganisaatioRepositorySpec extends FreeSpec with Matchers {
  "Hakee varhaiskasvatushierarkian" in {
    val hierarkias = OrganisaatioHierarkia.flatten(MockOrganisaatioRepository.findVarhaiskasvatusHierarkiat)
    val leafs = hierarkias.filter(o => o.children.isEmpty)
    leafs.flatMap(_.organisaatiotyypit).distinct should be(List(MockOrganisaatioRepository.varhaiskasvatusToimipaikkaTyyppi))
  }
}
