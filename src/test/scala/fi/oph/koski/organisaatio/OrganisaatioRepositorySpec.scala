package fi.oph.koski.organisaatio

import org.scalatest.{FreeSpec, Matchers}

class OrganisaatioRepositorySpec extends FreeSpec with Matchers {
  "Hakee varhaiskasvatushierarkian" in {
    val varhaiskasvatustoimipisteet = OrganisaatioHierarkia.flatten(MockOrganisaatioRepository.findVarhaiskasvatusHierarkiat).filter(_.children.isEmpty)
    varhaiskasvatustoimipisteet.flatMap(_.organisaatiotyypit).distinct should be(List("VARHAISKASVATUKSEN_TOIMIPAIKKA"))
  }
}
