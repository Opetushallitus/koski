package fi.oph.koski.perftest

object MixedPerfTester extends App {
  PerfTestRunner.executeTest(MixedOpiskeluoikeusInserterUpdaterScenario, RandomOpiskeluoikeusGetterScenario)
}
