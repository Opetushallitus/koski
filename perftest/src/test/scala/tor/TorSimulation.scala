package tor

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import tor.Scenarios.openFrontPage
import scala.concurrent.duration._


class TorSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://tordev.tor.oph.reaktor.fi/tor/")
    .acceptEncodingHeader("gzip, deflate")

  val headers = Map("Content-Type" -> """application/x-www-form-urlencoded""")

  setUp(openFrontPage.inject(atOnceUsers(1)).protocols(httpConf))
}

object Scenarios {
  val openFrontPage = scenario("Open frontpage").exec(http("frontpage").get("/"))
}
