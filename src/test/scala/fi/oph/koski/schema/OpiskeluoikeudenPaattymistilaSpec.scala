package fi.oph.koski.schema

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeudenPaattymistilaSpec extends AnyFreeSpec with Matchers {
  "korkeakoulu" - {
    "tunnistaa koodiarvon 7 (päättynyt yhden opiskeluoikeuden säännöksen johdosta) päättyneeksi tilaksi" in {
      OpiskeluoikeudenPäättymistila.korkeakoulu("7") shouldBe true
    }

    "tunnistaa jokaisen koodiston \"virtaopiskeluoikeudentila\" koodiarvon, eikä heitä poikkeusta" in {
      val koodisto = MockKoodistoViitePalvelu.getLatestVersionRequired("virtaopiskeluoikeudentila")
      val koodiarvot = MockKoodistoViitePalvelu.getKoodistoKoodiViitteet(koodisto).map(_.koodiarvo)

      koodiarvot should not be empty
      koodiarvot.foreach { koodiarvo =>
        noException should be thrownBy OpiskeluoikeudenPäättymistila.korkeakoulu(koodiarvo)
      }
    }
  }
}
