package fi.oph.koski.log

import fi.oph.koski.koodisto.KoodistoViite
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LoggableSpec extends AnyFreeSpec with Matchers {
  "Loggable.describe" - {
    "Booleans" in {
      Loggable.describe(java.lang.Boolean.valueOf("true")) should equal("true")
      Loggable.describe(true) should equal("true")
    }

    "Numbers" in {
      Loggable.describe(1) should equal("1")
    }

    "Options" in {
      Loggable.describe(Some(1)) should equal("Some(1)")
    }

    "Loggables" in {
      Loggable.describe(KoodistoViite("koulutus", 1)) should equal("koulutus/1")
    }

    "Others as _" in {
      Loggable.describe(new Thread()) should equal("_")
    }
  }
}
