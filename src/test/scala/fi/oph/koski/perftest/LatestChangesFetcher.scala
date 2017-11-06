package fi.oph.koski.perftest

import java.time.LocalDateTime

object LatestChangesFetcher extends App {
  PerfTestRunner.executeTest(FetchLatestChangesScenario)
}

object FetchLatestChangesScenario extends PerfTestScenario {
  override def operation(round: Int): List[Operation] = {
    List(Operation(uri = "api/oppija", queryParams = List(("muuttunutJälkeen", LocalDateTime.now.toString))))
  }
}