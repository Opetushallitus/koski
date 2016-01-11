package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._


class OverloadSimulation extends TorSimulation {

  setUp(
    findOppija.inject(atOnceUsers(1), nothingFor(10 seconds), constantUsersPerSec(100) during(1 minute) randomized)
  ).protocols(httpConf)
}