package fi.oph.koski.fixture

import com.typesafe.config.Config
import fi.oph.koski.config.Environment

class ValidationTestContext(config: Config) {
  private var validoiOpiskeluoikeudetState: Boolean = true

  def validoiOpiskeluoikeudet: Boolean = validoiOpiskeluoikeudetState

  def runWithoutValidations[T](f: => T): T =
    runTestHarness(f) {
      validoiOpiskeluoikeudetState = false
    } {
      validoiOpiskeluoikeudetState = true
    }

  private def runTestHarness[T](f: => T)(setup: => Unit)(reset: => Unit): T = {
    if (!Environment.isMockEnvironment(config)) {
      throw new RuntimeException("Validaatioita ei voi ohittaa tuotantoympäristössä")
    }

    synchronized {
      setup
      try {
        f
      }
      finally {
        reset
      }
    }
  }
}
