package fi.oph.koski.perftest

object MixedPerfTester extends App {
  PerfTestRunner.executeTest(MixedOpiskeluoikeusInserterScenario, RandomOpiskeluoikeusGetterScenario)
}
