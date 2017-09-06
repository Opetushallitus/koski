package fi.oph.koski.perftest

import fi.oph.koski.editor.EditorPerfTester.timed

object LocalPerfTest {
  def runTest(testCase: TestCase) = {
    testCase.round(0)
    timed(s"${testCase.rounds} rounds ${testCase.name}") {
      (1 to testCase.rounds).foreach(testCase.round)
    }
  }

  case class TestCase(name: String, rounds: Int, round: (Int) => Unit)
}
