package fi.oph.koski.organisaatio

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.{FreeSpec, Matchers}

class OrganisaatioRepositoryTest extends FreeSpec with Matchers {
  "OrganisaatioRepository" - {
    "Löytää organisaatio-oidit organisaation hierarkiasta" in {
      KoskiApplicationForTests.organisaatioRepository.getChildOids("1.2.246.562.10.93135224694").toList.flatten.length should equal(6)
    }
  }
}
