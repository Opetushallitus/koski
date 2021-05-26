package fi.oph.koski.perftest


object ValpasPeruskouluFromOidsOpiskeluoikeusInserter extends App {
  PerfTestRunner.executeTest(ValpasFromOidsOpiskeluoikeusInserterScenario)
}

object ValpasFromOidsOpiskeluoikeusInserterScenario extends ValpasOpiskeluoikeusInserterScenario with FixtureOidDataInserterScenario {
  override val luokka = "9A"
}
