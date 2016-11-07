package koski

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulatedScenarioBuilder
import io.gatling.http.Predef._

trait KoskiSimulation extends Simulation {
  val baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "https://dev.koski.opintopolku.fi/koski")

  val httpConf = http.baseURL(baseUrl).acceptEncodingHeader("gzip, deflate")

  val headers = Map("Content-Type" -> "application/json")

  override def setUp(scenarios: List[PopulatedScenarioBuilder]) = {
    super.setUp(scenarios).protocols(httpConf).assertions(
      global.successfulRequests.percent.is(100)
    )
  }
}
