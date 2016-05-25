package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class OverloadSimulation extends KoskiSimulation {
  setUp(
    prepareForFind.inject(atOnceUsers(1)),
    findOppija.inject(nothingFor(60 seconds), constantUsersPerSec(100) during(1 minute) randomized)
  )
}