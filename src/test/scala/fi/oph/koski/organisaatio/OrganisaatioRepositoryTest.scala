package fi.oph.koski.organisaatio

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OrganisaatioRepositoryTest extends AnyFreeSpec with Matchers {
  "OrganisaatioRepository" - {
    "Löytää organisaatio-oidit organisaation hierarkiasta" in {
      KoskiApplicationForTests.organisaatioRepository.getChildOids(MockOrganisaatiot.winnova).toList.flatten.length should equal(7)
    }
  }
}
