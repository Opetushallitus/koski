package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class UpdateSimulation extends TorSimulation {
  setUp(
    updateOppija.inject(nothingFor(5 seconds), atOnceUsers(1), nothingFor(30 seconds), constantUsersPerSec(10) during(1 minute) randomized)
  ).protocols(httpConf)
}


