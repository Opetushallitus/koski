package tor

import io.gatling.core.Predef._
import io.gatling.core.structure.PopulatedScenarioBuilder
import io.gatling.http.Predef._

trait KoskiSimulation extends Simulation {
  val baseUrl = sys.env.getOrElse("TOR_BASE_URL", "http://tordev.tor.oph.reaktor.fi/koski")

  val httpConf = http.baseURL(baseUrl).acceptEncodingHeader("gzip, deflate")

  val headers = Map("Content-Type" -> "application/json")

  override def setUp(scenarios: List[PopulatedScenarioBuilder]) = {
    super.setUp(scenarios).protocols(httpConf).assertions(
      global.successfulRequests.percent.is(100)
    )
  }
}
