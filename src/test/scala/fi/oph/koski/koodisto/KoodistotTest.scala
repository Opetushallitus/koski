package fi.oph.koski.koodisto

import org.scalatest.{FreeSpec, Matchers}

class KoodistotTest extends FreeSpec with Matchers {
  "Koski-koodistot" - {
    Koodistot.koskiKoodistot.foreach { koodistoUri =>
      koodistoUri in {
        getKoodisto(koodistoUri).codesGroupUri should equal("http://koski")
      }
    }
  }

  "Muut koodistot" - {
    Koodistot.muutKoodistot.foreach { koodistoUri =>
      koodistoUri in {
        getKoodisto(koodistoUri).codesGroupUri should not equal("http://koski")
      }
    }
  }

  private def getKoodisto(koodistoUri: String) = {
    val versio = MockKoodistoPalvelu().getLatestVersion(koodistoUri).get
    MockKoodistoPalvelu().getKoodisto(versio).get
  }
}
