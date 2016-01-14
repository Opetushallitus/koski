package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class NormalSimulation extends TorSimulation {
  setUp(
    findOppija.inject(atOnceUsers(1), nothingFor(10 seconds), constantUsersPerSec(1) during(1 minute) randomized),
    updateOppija.inject(nothingFor(5 seconds), atOnceUsers(1), nothingFor(10 seconds), constantUsersPerSec(1) during(1 minute) randomized),
    queryOppijat.inject(nothingFor(10 seconds), atOnceUsers(1), nothingFor(10 seconds), constantUsersPerSec(0.1) during(1 minute) randomized)
  )
}


