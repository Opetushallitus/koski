package fi.oph.koski.config

class ValidationContext {
  private var validoiOpiskeluoikeudetState: Boolean = true
  private var tarkastaOpiskeluoikeuksienDuplikaatitState: Boolean = true

  def validoiOpiskeluoikeudet: Boolean = validoiOpiskeluoikeudetState
  def tarkastaOpiskeluoikeuksienDuplikaatit: Boolean = tarkastaOpiskeluoikeuksienDuplikaatitState

  def runWithoutValidations[T](f: => T): T =
    runTestHarness(f) {
      validoiOpiskeluoikeudetState = false
    } {
      validoiOpiskeluoikeudetState = true
    }


  def runWithoutOpiskeluoikeuksienDuplikaattitarkistus[T](f: => T): T =
    runTestHarness(f) {
      tarkastaOpiskeluoikeuksienDuplikaatitState = false
    } {
      tarkastaOpiskeluoikeuksienDuplikaatitState = true
    }

  private def runTestHarness[T](f: => T)(setup: => Unit)(reset: => Unit) = {
    setup
    try { f }
    finally { reset }
  }
}
