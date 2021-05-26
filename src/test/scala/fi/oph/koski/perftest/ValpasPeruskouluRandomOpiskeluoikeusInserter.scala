package fi.oph.koski.perftest

object ValpasPeruskouluRandomOpiskeluoikeusInserter extends App {
  PerfTestRunner.executeTest(ValpasRandomOpiskeluoikeusInserterScenario)
}

object ValpasRandomOpiskeluoikeusInserterScenario extends ValpasOpiskeluoikeusInserterScenario with FixtureDataInserterScenario {
  override val hetu = new RandomHetu(2014)
  override val luokka = "9C"
}
