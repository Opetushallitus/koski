package fi.oph.koski.organisaatio

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OrganisaatioRepositorySpec extends AnyFreeSpec with Matchers {
  "Hakee varhaiskasvatushierarkian" in {
    val varhaiskasvatustoimipisteet = OrganisaatioHierarkia.flatten(MockOrganisaatioRepository.findVarhaiskasvatusHierarkiat).filter(_.children.isEmpty)
    varhaiskasvatustoimipisteet.flatMap(_.organisaatiotyypit).distinct should be(List("VARHAISKASVATUKSEN_TOIMIPAIKKA"))
  }
}
